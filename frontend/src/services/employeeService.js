import api from './api';

export const getEmployees = () => api.get('/employees');
export const getEmployee = (id) => api.get(`/employees/${id}`);
export const createEmployee = (payload) => api.post('/employees', payload);
export const updateEmployee = (id, payload) => api.put(`/employees/${id}`, payload);
export const setEmployeeEnabled = (id, enabled) => api.patch(`/employees/${id}/status`, { enabled });
export const deleteEmployee = (id) => api.delete(`/employees/${id}`);
