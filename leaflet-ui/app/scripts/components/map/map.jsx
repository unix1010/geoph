import React from 'react';
import L from 'leaflet';
import { connect } from 'react-redux';
import { loadProjects, updateBounds } from '../../actions/map.js';
import { Map, TileLayer } from 'react-leaflet';
import SvgLayer from './layers/svg.jsx';
import ClusteredLayer from './layers/clusteredLayer.jsx';
import ProjectPopup from './popups/projectLayerPopup';
import SimplePopup from './popups/simplePopup';
import PhotoPopup from './popups/photoPopup';
import {getVisibles} from '../../util/layersUtil.js';
import Legends from './legends/legends';

require('leaflet/dist/leaflet.css');
require('./map.scss');

const MapView = React.createClass({
  
  getInitialState() {
    return {bounds: {}};
  },
  
  handleChangeBounds(e) {
    this.props.onUpdateBounds(
      e.target.getBounds(),
      [e.target.getCenter().lat, e.target.getCenter().lng],
      e.target.getZoom());
  },
  
  closePopup() {
    let map = this.refs.map;
    if (map) {
      map.leafletElement.closePopup();
    }
  },
  
  getPopUp(id) {
    const { mapId } = this.props;
    
    if (id === "projectPopup") {
      return (<ProjectPopup mapId={mapId} onClosePopup={this.closePopup}/>)
    }
    if (id = "defaultPopup") {
      return (<SimplePopup mapId={mapId} onClosePopup={this.closePopup}/>)
    }
    
    if (id = "photoPopup") {
      return <PhotoPopup mapId={mapId} onClosePopup={this.closePopup}/>
    }
  },
  
  getLayer(l) {
    const {data, type, popupId, id, zIndex, settings} = l;
    const {showLabels} = settings || {};
    
    if (type === 'clustered') {
      return (
        <ClusteredLayer key={id} data={data}>
          <PhotoPopup onClosePopup={this.closePopup}/>
        </ClusteredLayer>
      );
    } else {
      return (
        <SvgLayer showLabels={showLabels} key={id} id={id} zIndex={zIndex} features={data.features}>
          {this.getPopUp(popupId)}
        </SvgLayer>
      )
    }
  },
  
  render() {
    const { map, mapId } = this.props;
    
    const { southWest, northEast } = map.get('defaultBounds').toJS();
    const center = map.get('center') !== undefined ? map.get('center').toJS() : undefined;
    const zoom = map.get('zoom');
    
    // don't use the bounds if we have a zoom level and a center
    const bounds = center === undefined ? L.latLngBounds(L.latLng(southWest.lat, southWest.lng), L.latLng(northEast.lat, northEast.lng)) : undefined;
    
    let layers = getVisibles(this.props.map.get('layers')).toJS();
    let loading = map.get('loading');
    
    return (
      <div className={loading ? "half-opacity" : ""}>
        <Map ref="map" className="map" center={center} zoom={zoom} bounds={bounds} onMoveEnd={this.handleChangeBounds}>
          <TileLayer url={this.props.map.get('basemap').get('url')}/>
          {layers.map((l) => {
            const {data, type} = l;
            return (data && data.features) ? this.getLayer(l) : null;
          })}
        </Map>
        <Legends mapId={mapId} layers={this.props.map.get('layers')}/>
        {loading
          ? <div className="loading-map">
            <div className="loading-css">
              <div></div>
            </div>
          </div>
          : null}
      </div>
    );
  }
});

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    onUpdateBounds: (newBounds, newCenter, newZoom) => {
      dispatch(updateBounds(newBounds, newCenter, newZoom));
    }
  }
};

const stateToProps = (state, props) => {
  const { mapId } = props;
  
  let map;
  if (mapId === 'main') {
    map = state.map;
  } else {
    // here id should be 'left'
    map = state.compare.get("map");
  }
  
  return {
    map: map,
    fundingType: state.settings.fundingType
  };
};

export default connect(stateToProps, mapDispatchToProps)(MapView);

