package org.auscope.portal.gsml;

import java.util.ArrayList;
import java.util.List;

import org.auscope.portal.core.services.methodmakers.filter.AbstractFilter;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.service.colorcoding.CapdfHydroChemColorCoding;


/**
 * Class that represents ogc:Filter markup for mt:mineralTenement queries
 *
 * @author Victor Tey
 * @version
 */
public class TIMAGeosampleFilter extends AbstractFilter {
    List<String> fragments;

    /**
     * Given a mine name, this object will build a filter to a wild card search
     * for mine names
     *
     * @param mineName
     *            the main name
     */
    public TIMAGeosampleFilter(String name,String igsn) {

        fragments = new ArrayList<String>();
        if (name != null && !name.isEmpty()) {
            fragments.add(this.generatePropertyIsLikeFragment("name", name));
        }

        if (igsn != null && !igsn.isEmpty()) {
            fragments.add(this.generatePropertyIsLikeFragment("igsn", igsn));
        }



    }


    @Override
    public String getFilterStringAllRecords() {
        return this.generateFilter(this.generateAndComparisonFragment(fragments.toArray(new String[fragments.size()])));
    }

    @Override
    public String getFilterStringBoundingBox(FilterBoundingBox bbox) {

        List<String> localFragment = new ArrayList<String>(fragments);
        localFragment.add(this.generateBboxFragment(bbox, "location"));

        return this.generateFilter(this.generateAndComparisonFragment(localFragment.toArray(new String[localFragment.size()])));
    }


}


