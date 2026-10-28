import { ConfigAction, FETCH_CONFIG_FAILURE, FETCH_CONFIG_REQUEST, FETCH_CONFIG_SUCCESS } from './actions';
import { ConfigState } from './types';

const initialState: ConfigState = {
  bizName: [],
  systemType: [],
  processStatus: [],
  matchedRule: [],
  riskLevel: [],
  eventStatus: [],
  loaded: false,
  loading: false,
  error: null
};

const configReducer = (state = initialState, action: ConfigAction): ConfigState => {
  switch (action.type) {
    case FETCH_CONFIG_REQUEST:
      return { ...state, loading: true, error: null };
    case FETCH_CONFIG_SUCCESS:
      return {
        ...state,
        bizName: action.payload.bizName || [],
        systemType: action.payload.system_type || [],
        processStatus: action.payload.processStatus || [],
        matchedRule: action.payload.matchedRule || [],
        riskLevel: action.payload.riskLevel || [],
        eventStatus: action.payload.eventStatus || [],
        loaded: true,
        loading: false
      };
    case FETCH_CONFIG_FAILURE:
      return { ...state, loading: false, error: action.payload };
    default:
      return state;
  }
};

export default configReducer;