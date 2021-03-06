package org.devgateway.geoph.core.services;

import org.devgateway.geoph.core.response.IndicatorResponse;
import org.devgateway.geoph.model.Indicator;

import java.util.List;

/**
 * @author dbianco
 *         created on abr 25 2016.
 */
public interface LayerService {

    List<Indicator> getIndicatorsList();

    IndicatorResponse getIndicatorById(Long id);

    void deleteIndicator(Long id);

}
