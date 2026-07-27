import api from './api';

export const getAllCategories = () => api.get('/policy-categories');
export const getActiveCategories = () => api.get('/policy-categories/active');
export const createCategory = (payload) => api.post('/policy-categories', payload);
export const updateCategory = (id, payload) => api.put(`/policy-categories/${id}`, payload);
export const setCategoryActive = (id, active) => api.patch(`/policy-categories/${id}/status`, { active });
