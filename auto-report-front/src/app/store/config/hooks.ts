import { useSelector } from 'react-redux';
import { RootState } from 'app/store/rootReducer';
import { ConfigState } from './types';

export const useConfig = (): ConfigState => useSelector((state: RootState) => state.config);