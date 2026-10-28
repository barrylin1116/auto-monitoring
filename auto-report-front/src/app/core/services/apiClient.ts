import axios from 'axios';

const apiClient = axios.create({
  baseURL: '',
  timeout: 15000
});

export default apiClient;