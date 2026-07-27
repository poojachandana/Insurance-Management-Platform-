import api from './api';

export const getMyNotifications = () => api.get('/notifications/me');
export const getUnreadCount = () => api.get('/notifications/me/unread-count');
export const markNotificationRead = (id) => api.patch(`/notifications/${id}/read`);
export const markAllNotificationsRead = () => api.patch('/notifications/read-all');
