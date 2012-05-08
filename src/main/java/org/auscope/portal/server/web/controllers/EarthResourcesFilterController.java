package org.auscope.portal.server.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.domain.wfs.WFSCountResponse;
import org.auscope.portal.server.domain.wfs.WFSKMLResponse;
import org.auscope.portal.server.web.service.MineralOccurrenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;


/**
 * Controller that handles all Earth Resource related requests
 * <p>
 * It handles the following WFS features:
 * <ul>
 * <li>Mine</li>
 * <li>Mineral Occurrence</li>
 * <li>Mininig Activity</li>
 * </ul>
 * </p>
 *
 * @author Jarek Sanders
 * @author Josh Vote
 */
@Controller
public class EarthResourcesFilterController extends BasePortalController {

    // ----------------------------------------------------- Instance variables

    private MineralOccurrenceService mineralOccurrenceService;

    // ----------------------------------------------------------- Constructors

    @Autowired
    public EarthResourcesFilterController(MineralOccurrenceService mineralOccurrenceService) {
        this.mineralOccurrenceService = mineralOccurrenceService;
    }

    // ------------------------------------------- Property Setters and Getters


    /**
     * Handles the Earth Resource Mine filter queries.
     * (If the bbox elements are specified, they will limit the output response to 200 records implicitly)
     *
     * @param serviceUrl the url of the service to query
     * @param mineName   the name of the mine to query for
     * @param request    the HTTP client request
     * @return a WFS response converted into KML
     * @throws Exception
     */
    @RequestMapping("/doMineFilter.do")
    public ModelAndView doMineFilter(
            @RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("mineName") String mineName,
            @RequestParam(required=false, value="bbox") String bboxJson,
            @RequestParam(required=false, value="maxFeatures", defaultValue="0") int maxFeatures) throws Exception {

        //The presence of a bounding box causes us to assume we will be using this GML for visualizing on a map
        //This will in turn limit the number of points returned to 200
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);

        try {
            WFSKMLResponse response = this.mineralOccurrenceService.getMinesGml(serviceUrl, mineName, bbox, maxFeatures);

            return generateJSONResponseMAV(true, response.getGml(), response.getKml(), response.getMethod());
        } catch (Exception e) {
            log.warn(String.format("Error performing filter for '%1$s': %2$s", serviceUrl, e));
            log.debug("Exception: ", e);
            return this.generateExceptionResponse(e, serviceUrl);
        }
    }

    /**
     * Handles getting the count of the Earth Resource Mine filter queries.
     * (If the bbox elements are specified, they will limit the output response to 200 records implicitly)
     *
     * @param serviceUrl the url of the service to query
     * @param mineName   the name of the mine to query for
     * @param request    the HTTP client request
     * @return a WFS response converted into KML
     * @throws Exception
     */
    @RequestMapping("/doMineFilterCount.do")
    public ModelAndView doMineFilterCount(
            @RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("mineName") String mineName,
            @RequestParam(required=false, value="bbox") String bboxJson,
            @RequestParam(required=false, value="maxFeatures", defaultValue="0") int maxFeatures) throws Exception {

        //The presence of a bounding box causes us to assume we will be using this GML for visualizing on a map
        //This will in turn limit the number of points returned to 200
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);

        try {
            WFSCountResponse response = this.mineralOccurrenceService.getMinesCount(serviceUrl, mineName, bbox, maxFeatures);
            return generateJSONResponseMAV(true, new Integer(response.getNumberOfFeatures()), "");
        } catch (Exception e) {
            log.warn(String.format("Error performing filter for '%1$s': %2$s", serviceUrl, e));
            log.debug("Exception: ", e);
            return this.generateExceptionResponse(e, serviceUrl);
        }
    }


    /**
     * Handles the Earth Resource MineralOccerrence filter queries.
     *
     * @param serviceUrl
     * @param commodityName
     * @param measureType
     * @param minOreAmount
     * @param minOreAmountUOM
     * @param minCommodityAmount
     * @param minCommodityAmountUOM
     * @param request                the HTTP client request
     *
     * @return a WFS response converted into KML
     * @throws Exception
     */
    @RequestMapping("/doMineralOccurrenceFilter.do")
    public ModelAndView doMineralOccurrenceFilter(
        @RequestParam(value="serviceUrl",            required=false) String serviceUrl,
        @RequestParam(value="commodityName",         required=false) String commodityName,
        @RequestParam(value="measureType",           required=false) String measureType,
        @RequestParam(value="minOreAmount",          required=false) String minOreAmount,
        @RequestParam(value="minOreAmountUOM",       required=false) String minOreAmountUOM,
        @RequestParam(value="minCommodityAmount",    required=false) String minCommodityAmount,
        @RequestParam(value="minCommodityAmountUOM", required=false) String minCommodityAmountUOM,
        @RequestParam(required=false, value="bbox") String bboxJson,
        @RequestParam(required=false, value="maxFeatures", defaultValue="0") int maxFeatures) throws Exception {
        //The presence of a bounding box causes us to assume we will be using this GML for visualising on a map
        //This will in turn limit the number of points returned to 200
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);

        try {
            //get the mineral occurrences
            WFSKMLResponse response = this.mineralOccurrenceService.getMineralOccurrenceGml(
                    serviceUrl,
                    commodityName,
                    measureType,
                    minOreAmount,
                    minOreAmountUOM,
                    minCommodityAmount,
                    minCommodityAmountUOM,
                    maxFeatures,
                    bbox);

            return generateJSONResponseMAV(true, response.getGml(), response.getKml(), response.getMethod());
        } catch (Exception e) {
            log.warn(String.format("Error performing filter for '%1$s': %2$s", serviceUrl, e));
            log.debug("Exception: ", e);
            return this.generateExceptionResponse(e, serviceUrl);

        }
    }

    /**
     * Handles counting the results of a Earth Resource MineralOccerrence filter query.
     *
     * @param serviceUrl
     * @param commodityName
     * @param measureType
     * @param minOreAmount
     * @param minOreAmountUOM
     * @param minCommodityAmount
     * @param minCommodityAmountUOM
     * @param request                the HTTP client request
     *
     * @return Returns Integer count
     * @throws Exception
     */
    @RequestMapping("/doMineralOccurrenceFilterCount.do")
    public ModelAndView doMineralOccurrenceFilterCount(
        @RequestParam(value="serviceUrl",            required=false) String serviceUrl,
        @RequestParam(value="commodityName",         required=false) String commodityName,
        @RequestParam(value="measureType",           required=false) String measureType,
        @RequestParam(value="minOreAmount",          required=false) String minOreAmount,
        @RequestParam(value="minOreAmountUOM",       required=false) String minOreAmountUOM,
        @RequestParam(value="minCommodityAmount",    required=false) String minCommodityAmount,
        @RequestParam(value="minCommodityAmountUOM", required=false) String minCommodityAmountUOM,
        @RequestParam(required=false, value="bbox") String bboxJson,
        @RequestParam(required=false, value="maxFeatures", defaultValue="0") int maxFeatures) throws Exception {
        //The presence of a bounding box causes us to assume we will be using this GML for visualising on a map
        //This will in turn limit the number of points returned to 200
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);

        try {
            //get the mineral occurrences
            WFSCountResponse response = this.mineralOccurrenceService.getMineralOccurrenceCount(
                    serviceUrl,
                    commodityName,
                    measureType,
                    minOreAmount,
                    minOreAmountUOM,
                    minCommodityAmount,
                    minCommodityAmountUOM,
                    maxFeatures,
                    bbox);

            return generateJSONResponseMAV(true, new Integer(response.getNumberOfFeatures()), "");
        } catch (Exception e) {
            log.warn(String.format("Error performing filter for '%1$s': %2$s", serviceUrl, e));
            log.debug("Exception: ", e);
            return this.generateExceptionResponse(e, serviceUrl);

        }
    }

    @RequestMapping("/doMiningActivityFilterStyle.do")
    public void baconTestCode(
            HttpServletResponse response,
            @RequestParam(required=false, value="mineName", defaultValue="")         String mineName,
            @RequestParam(required=false, value="startDate", defaultValue="")        String startDate,
            @RequestParam(required=false, value="endDate", defaultValue="")          String endDate,
            @RequestParam(required=false, value="oreProcessed", defaultValue="")     String oreProcessed,
            @RequestParam(required=false, value="producedMaterial", defaultValue="") String producedMaterial,
            @RequestParam(required=false, value="cutOffGrade", defaultValue="")      String cutOffGrade,
            @RequestParam(required=false, value="production", defaultValue="")       String production,
            @RequestParam(required=false, value="bbox", defaultValue="")             String bboxJson,
            @RequestParam(required=false, value="maxFeatures", defaultValue="0")     int maxFeatures)
    throws Exception
    {
        FilterBoundingBox bbox = null;

        // Get the mining activities
        String filter = this.mineralOccurrenceService.getMiningActivityFilter(mineName
                , startDate
                , endDate
                , oreProcessed
                , producedMaterial
                , cutOffGrade
                , production
                , maxFeatures
                , bbox);

        String style = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<StyledLayerDescriptor version=\"1.0.0\" xmlns:er=\"urn:cgi:xmlns:GGIC:EarthResource:1.1\" xsi:schemaLocation=\"http://www.opengis.net/sld StyledLayerDescriptor.xsd\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gsml=\"urn:cgi:xmlns:CGI:GeoSciML:2.0\" xmlns:sld=\"http://www.opengis.net/sld\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                "<NamedLayer>" +
                "<Name>er:MiningFeatureOccurrence</Name>" +
                "<UserStyle>" +
                "<Name>portal-style</Name>" +
                "<Title>portal-style</Title>" +
                "<Abstract>portal-style</Abstract>" +
                "<IsDefault>1</IsDefault>" +
                "<FeatureTypeStyle>" +
                "<Rule>" +
                "<Name>portal-style</Name>" +
                "<Abstract>portal-style</Abstract>" +
                filter +
                "<PointSymbolizer>" +
                "<Graphic>" +
                "<Mark>" +
                "<WellKnownName>square</WellKnownName>" +
                "<Fill>" +
                "<CssParameter name=\"fill\">#0000FF</CssParameter>" +
                "</Fill>" +
                "</Mark>" +
                "<Size>6</Size>" +
                "</Graphic>" +
                "</PointSymbolizer>" +
                "</Rule>" +
                "</FeatureTypeStyle>" +
                "</UserStyle>" +
                "</NamedLayer>" +
                "</StyledLayerDescriptor>";

        log.error("style=" + style);

        response.setContentType("text/xml");

        ByteArrayInputStream styleStream = new ByteArrayInputStream(style.getBytes());
        OutputStream outputStream = response.getOutputStream();

        this.writeInputToOutputStream(styleStream, outputStream, 1024);

        styleStream.close();
        outputStream.close();
    }

    /**
     * Handles Mining Activity filter queries
     * Returns WFS response converted into KML.
     *
     * @param serviceUrl
     * @param mineName
     * @param startDate
     * @param endDate
     * @param oreProcessed
     * @param producedMaterial
     * @param cutOffGrade
     * @param production
     * @param request
     * @return the KML response
     * @throws Exception
     */
    @RequestMapping("/doMiningActivityFilter.do")
    public ModelAndView doMiningActivityFilter(
            @RequestParam("serviceUrl")       String serviceUrl,
            @RequestParam(required=false, value="mineName", defaultValue="")         String mineName,
            @RequestParam(required=false, value="startDate", defaultValue="")        String startDate,
            @RequestParam(required=false, value="endDate", defaultValue="")          String endDate,
            @RequestParam(required=false, value="oreProcessed", defaultValue="")     String oreProcessed,
            @RequestParam(required=false, value="producedMaterial", defaultValue="") String producedMaterial,
            @RequestParam(required=false, value="cutOffGrade", defaultValue="")      String cutOffGrade,
            @RequestParam(required=false, value="production", defaultValue="")       String production,
            @RequestParam(required=false, value="bbox", defaultValue="")             String bboxJson,
            @RequestParam(required=false, value="maxFeatures", defaultValue="0")     int maxFeatures)
    throws Exception
    {
        //The presence of a bounding box causes us to assume we will be using this GML for visualizing on a map
        //This will in turn limit the number of points returned to 200
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);

        try {
            // Get the mining activities
            WFSKMLResponse response = this.mineralOccurrenceService.getMiningActivityGml(serviceUrl
                    , mineName
                    , startDate
                    , endDate
                    , oreProcessed
                    , producedMaterial
                    , cutOffGrade
                    , production
                    , maxFeatures
                    , bbox);

            return generateJSONResponseMAV(true, response.getGml(), response.getKml(), response.getMethod());
        } catch (Exception e) {
            log.warn(String.format("Error performing filter for '%1$s': %2$s", serviceUrl, e));
            log.debug("Exception: ", e);
            return this.generateExceptionResponse(e, serviceUrl);
        }
    }

    /**
     * Handles counting the number Mining Activities matched by a filter
     * Returns Integer count
     *
     * @param serviceUrl
     * @param mineName
     * @param startDate
     * @param endDate
     * @param oreProcessed
     * @param producedMaterial
     * @param cutOffGrade
     * @param production
     * @param request
     * @return Returns Integer count
     * @throws Exception
     */
    @RequestMapping("/doMiningActivityFilterCount.do")
    public ModelAndView doMiningActivityFilterCount(
            @RequestParam("serviceUrl")       String serviceUrl,
            @RequestParam(required=false, value="mineName", defaultValue="")         String mineName,
            @RequestParam(required=false, value="startDate", defaultValue="")        String startDate,
            @RequestParam(required=false, value="endDate", defaultValue="")          String endDate,
            @RequestParam(required=false, value="oreProcessed", defaultValue="")     String oreProcessed,
            @RequestParam(required=false, value="producedMaterial", defaultValue="") String producedMaterial,
            @RequestParam(required=false, value="cutOffGrade", defaultValue="")      String cutOffGrade,
            @RequestParam(required=false, value="production", defaultValue="")       String production,
            @RequestParam(required=false, value="bbox")                              String bboxJson,
            @RequestParam(required=false, value="maxFeatures", defaultValue="0")     int maxFeatures) throws Exception {

        //The presence of a bounding box causes us to assume we will be using this GML for visualizing on a map
        //This will in turn limit the number of points returned to 200
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);

        try {
            // Get the mining activities
            WFSCountResponse response = this.mineralOccurrenceService.getMiningActivityCount(serviceUrl
                    , mineName
                    , startDate
                    , endDate
                    , oreProcessed
                    , producedMaterial
                    , cutOffGrade
                    , production
                    , maxFeatures
                    , bbox);

            return generateJSONResponseMAV(true, new Integer(response.getNumberOfFeatures()), "");
        } catch (Exception e) {
            log.warn(String.format("Error performing filter for '%1$s': %2$s", serviceUrl, e));
            log.debug("Exception: ", e);
            return this.generateExceptionResponse(e, serviceUrl);
        }
    }
}
