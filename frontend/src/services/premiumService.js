import api from './api';

export const getPremiums = () => api.get('/premiums');
export const getPremiumsPaged = (params) => api.get('/premiums/paged', { params });
export const getOverduePremiums = () => api.get('/premiums/overdue');
export const getMyPremiums = () => api.get('/premiums/me');
export const getPremiumsByPolicy = (policyId) => api.get(`/premiums/policy/${policyId}`);
export const createDuePremium = (payload) => api.post('/premiums', payload);
export const payPremium = (id) => api.patch(`/premiums/${id}/pay`);
