import api from './api';

export const getPolicies = () => api.get('/policies');
export const getPoliciesPaged = (params) => api.get('/policies/paged', { params });
export const getPoliciesByCustomer = (customerId) => api.get(`/policies/customer/${customerId}`);
export const getMyPolicies = () => api.get('/policies/me');
export const getPolicy = (id) => api.get(`/policies/${id}`);
export const createPolicy = (payload) => api.post('/policies', payload);
export const updatePolicy = (id, payload) => api.put(`/policies/${id}`, payload);
export const renewPolicy = (id, extendMonths = 12) => api.patch(`/policies/${id}/renew`, null, { params: { extendMonths } });
export const cancelPolicy = (id) => api.patch(`/policies/${id}/cancel`);
export const getExpiringPolicies = (days = 30) => api.get('/policies/expiring', { params: { days } });
