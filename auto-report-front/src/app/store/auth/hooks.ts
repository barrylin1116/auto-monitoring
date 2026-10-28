import { useSelector } from 'react-redux';
import { RootState } from 'app/store/rootReducer';
import { AuthState } from './types';

export const useAuth = (): AuthState => useSelector((state: RootState) => state.auth);
