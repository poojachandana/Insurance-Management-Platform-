import api from './api';

export const getCustomers = (search) => api.get('/customers', { params: search ? { search } : {} });
export const getCustomersPaged = (params) => api.get('/customers/paged', { params });
export const getCustomer = (id) => api.get(`/customers/${id}`);
export const getMyProfile = () => api.get('/customers/me');
export const updateMyProfile = (payload) => api.put('/customers/me', payload);
export const createCustomer = (payload) => api.post('/customers', payload);
export const updateCustomer = (id, payload) => api.put(`/customers/${id}`, payload);
export const deleteCustomer = (id) => api.delete(`/customers/${id}`);
export const assignCustomerAgent = (id, agentId) => api.patch(`/customers/${id}/assign-agent`, { agentId });