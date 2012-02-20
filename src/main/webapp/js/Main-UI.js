Ext.application({
    //Here we build our GUI from existing components - this function should only be assembling the GUI
    //Any processing logic should be managed in dedicated classes - don't let this become a
    //monolithic 'do everything' function
    launch : function() {
        var map = null; //an instance of a GMap2 object (google map v2 API)

        //Send these headers with every AJax request we make...
        Ext.Ajax.defaultHeaders = {
            'Accept-Encoding': 'gzip, deflate' //This ensures we use gzip for most of our requests (where available)
        };

        //Create our CSWRecord store (holds all CSWRecords not mapped by known layers)
        var unmappedCSWRecordStore = Ext.create('Ext.data.Store', {
            model : 'portal.csw.CSWRecord',
            groupField: 'contactOrg',
            proxy : {
                type : 'ajax',
                url : 'getUnmappedCSWRecords.do',
                reader : {
                    type : 'json',
                    root : 'data'
                }
            },
            autoLoad : true
        });

        //Our custom record store holds layers that the user has
        //added to the map using a OWS URL entered through the
        //custom layers panel
        var customRecordStore = Ext.create('Ext.data.Store', {
            model : 'portal.csw.CSWRecord',
            data : []
        });

        //Create our KnownLayer store
        var knownLayerStore = Ext.create('Ext.data.Store', {
            model : 'portal.knownlayer.KnownLayer',
            groupField: 'group',
            proxy : {
                type : 'ajax',
                url : 'getKnownLayers.do',
                reader : {
                    type : 'json',
                    root : 'data'
                }
            },
            autoLoad : true
        });

        /**
         * Used to show extra details for querying services
         */
        var filterPanel = Ext.create('portal.widgets.panel.FilterPanel', {
            region: 'south',
            split: true,
            height: 160
        });

        //Create our store for holding the set of
        //layers that have been added to the map
        var layerStore = Ext.create('portal.layer.LayerStore', {});

        var layersPanel = Ext.create('portal.widgets.panel.LayerPanel', {
            title : 'Active Layers',
            region : 'center',
            store : layerStore,
            filterPanel : filterPanel,
            listeners : {
                //On selection, update our filter panel
                select : function(rowModel, record, index) {
                    filterPanel.showFilterForLayer(record);
                },
                //Whenever the layer download is clicked
                downloadlayer : function(panel, layer) {
                    //We need a copy of the current filter object (in case the user
                    //has filled out filter options but NOT hit apply filter) and
                    //the original filter objects
                    var renderedFilterer = layer.get('filterer').clone();
                    var currentFilterer = Ext.create('portal.layer.filterer.Filterer', {});
                    var currentFilterForm = filterPanel.getFilterFormForLayer(layer);
                    if (currentFilterForm) {
                        currentFilterForm.writeToFilterer(currentFilterer);
                    }

                    //Finally pass off the download handling to the appropriate downloader (if it exists)
                    var downloader = layer.get('downloader');
                    if (downloader) {
                        var onlineResources = layer.getAllOnlineResources();
                        downloader.downloadData(onlineResources, renderedFilterer, currentFilterer);
                    }
                }
            }
        });

        //Utility function for adding a new layer to the map
        var handleAddRecordToMap = function(sourceGrid, record) {
            var layerFactory = Ext.create('portal.layer.LayerFactory', {});
            var newLayer = null;
            if (record instanceof portal.csw.CSWRecord) {
                newLayer = layerFactory.generateLayerFromCSWRecord(record);
            } else {
                newLayer = layerFactory.generateLayerFromKnownLayer(record);
            }

            layerStore.add(newLayer); //this adds the layer to our store
            layersPanel.getSelectionModel().select([newLayer], false); //this ensures it gets selected

            //Only force a render if the layer has no filter
            if (filterPanel.isFilteringDisabled()) {
                var renderer = newLayer.get('renderer');
                var filterer = newLayer.get('filterer');
                var resources = newLayer.getAllOnlineResources();

                renderer.displayData(resources, filterer, Ext.emptyFn);
            }


        };

        var knownLayersPanel = Ext.create('portal.widgets.panel.KnownLayerPanel', {
            title : 'Featured Layers',
            store : knownLayerStore,
            listeners : {
                addlayerrequest : handleAddRecordToMap
            }
        });

        var unmappedRecordsPanel = Ext.create('portal.widgets.panel.CSWRecordPanel', {
            title : 'Registered Layers',
            store : unmappedCSWRecordStore,
            listeners : {
                addlayerrequest : handleAddRecordToMap
            }
        });

        var customRecordsPanel = Ext.create('portal.widgets.panel.CSWRecordPanel', {
            title : 'Custom Layers',
            store : customRecordStore,
            listeners : {
                addlayerrequest : handleAddRecordToMap
            }
        });

        // basic tabs 1, built from existing content
        var tabsPanel = Ext.create('Ext.TabPanel', {
            activeTab : 0,
            region : 'north',
            split : true,
            height : 265,
            enableTabScroll : true,
            items:[knownLayersPanel,
                unmappedRecordsPanel,
                customRecordsPanel
            ]
        });

        /**
         * Used as a placeholder for the tree and details panel on the left of screen
         */
        var westPanel = {
            layout: 'border',
            region:'west',
            border: false,
            split:true,
            //margins: '100 0 0 0',
            margins:'100 0 0 3',
            width: 350,
            items:[tabsPanel , layersPanel, filterPanel]
        };

        /**
         * This center panel will hold the google maps instance
         */
        var centerPanel = new Ext.Panel({
            region: 'center',
            id: 'center_region',
            margins: '100 0 0 0',
            cmargins:'100 0 0 0'
        });

        /**
         * Add all the panels to the viewport
         */
        var viewport = new Ext.Viewport({
            layout:'border',
            items:[westPanel, centerPanel]
        });


        //Initialise our google map API
        portal.util.gmap.MapUtil.generateMap(centerPanel);

        //We need something to handle the clicks on the map
        var queryTargetHandler = Ext.create('portal.layer.querier.QueryTargetHandler', {
            container : centerPanel
        });

        //when updateCSWRecords person clicks on updateCSWRecords marker then do something
        portal.util.gmap.MapUtil.registerMapEvent("click", function(overlay, latlng, overlayLatlng) {
            //Figure out what we clicked and then pass that info along to appropriate layer queriers
            var queryTargets = portal.util.gmap.ClickController.generateQueryTargets(overlay, latlng, overlayLatlng, layerStore);
            queryTargetHandler.handleQueryTargets(queryTargets);
        });
    }
});