import api from './api';

export const getMyAccount = () => api.get('/account/me');
export const updateMyAccount = (payload) => api.put('/account/me', payload);
export const changeMyPassword = (payload) => api.put('/account/me/password', payload);
