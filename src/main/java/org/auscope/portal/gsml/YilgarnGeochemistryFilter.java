package org.auscope.portal.gsml;

import java.util.ArrayList;
import java.util.List;

import org.auscope.portal.core.services.methodmakers.filter.AbstractFilter;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;

/**
 * @author
 *
 * @version $Id: YilgarnGeochemistryFilter.java 1233 2010-10-20   $
 */

public class YilgarnGeochemistryFilter extends AbstractFilter {
    private String geologicName;

    // -------------------------------------------------------------- Constants


    // ----------------------------------------------------------- Constructors

    public YilgarnGeochemistryFilter(String geologicName) {
        this.geologicName = geologicName;
    }

    // --------------------------------------------------------- Public Methods

    @Override
    public String getFilterStringAllRecords() {
        return this.generateFilter(this.generateFilterFragment());
    }

    @Override
    public String getFilterStringBoundingBox(FilterBoundingBox bbox) {

        return this.generateFilter(
                this.generateAndComparisonFragment(
                        this.generateBboxFragment(bbox, "gsml:occurrence/gsml:MappedFeature/gsml:shape"),
                        this.generateFilterFragment()));
    }


    // -------------------------------------------------------- Private Methods
    private String generateFilterFragment() {
        List<String> parameterFragments = new ArrayList<String>();
        if (geologicName != null && geologicName.length() > 0) {
            parameterFragments.add(this.generatePropertyIsLikeFragment("gml:name", this.geologicName));
        }

        return this.generateAndComparisonFragment(
                this.generateAndComparisonFragment(parameterFragments.toArray(new String[parameterFragments.size()])));
    }
}
