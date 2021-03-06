import * as Constants from '../constants/constants';
import Immutable from 'immutable';

const defaultState = Immutable.fromJS(
  {id: '', name: '', description: '', type: 'dashboard', saving: false}
);

const saveMap = (state = defaultState, action) => {
  switch (action.type) {
    
    case Constants.LOAD_DEFAULT_MAP_STATE:
      return defaultState;
    
    case Constants.REQUEST_STATE_RESTORE:
      return state.set('mapKey', action.mapKey);
    
    case Constants.STATE_RESTORE:
      state = state.set('name', action.storedMap.name);
      state = state.set('description', action.storedMap.description);
      state = state.set('type', action.storedMap.type);
      return state.set('id', action.storedMap.id);
    
    case Constants.CHANGE_SAVE_PROPERTY:
      return state.set(action.property, action.value);
    
    case Constants.UPDATE_SAVE_ERRORS:
      return state.set('errors', action.errors);
    
    case Constants.REQUEST_SAVE_MAP:
      return state.set('saving', true);
    
    case Constants.REQUEST_SAVE_MAP_ERROR:
      state = state.set('saving', false);
      return state.set('httpError', action.httpError);
    
    case Constants.REQUEST_SAVE_MAP_OK:
      state = defaultState;
      return state;
    
    default:
      return state
  }
};

export default saveMap