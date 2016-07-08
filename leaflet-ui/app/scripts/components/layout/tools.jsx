import React from 'react';
import { connect } from 'react-redux'
import {LayerControl} from '../controls/layer';
import ExpandableControl from '../controls/expandableControl';
import ProjectFilter from '../filter/projectFilter';
import { loadDefaultLayers } from '../../actions/map';
import { collectValues } from '../../util/filterUtil';
require('./tools.scss');

class Tools extends React.Component {

  constructor() {
    super();
  }

  componentDidMount(){
    let filters = collectValues(this.props.filters, this.props.projectSearch);
    this.props.onLoadDefaultLayers(this.props.layers, filters);
  }

  render() {
    return (
    	<div className="tools-view">
        <hr/>
        <ExpandableControl title="Project Search" iconClass="search-icon">
          <div><ProjectFilter/></div>
        </ExpandableControl>
        <ExpandableControl title="Adjust layers to see detailed data" defaultExpanded={true}  iconClass="layers-icon">
          <div><LayerControl/></div>
        </ExpandableControl>        
      </div>
    )
  }
}

const stateToProps = (state, props) => {
  return {
    layers: state.map.get('layers'),
    filters: state.filters.filterMain,
    projectSearch: state.projectSearch
  };
}


const dispatchToProps = (dispatch, ownProps) => {
  return {
    onLoadDefaultLayers: (layers, filters) => {
      dispatch(loadDefaultLayers(layers, filters));
    }
  }
}

export default connect(stateToProps,dispatchToProps)(Tools);

