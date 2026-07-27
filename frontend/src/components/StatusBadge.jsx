import React from 'react';

const COLORS = {
  ACTIVE: 'bg-green-100 text-green-700',
  EXPIRED: 'bg-gray-200 text-gray-600',
  CANCELLED: 'bg-red-100 text-red-700',
  RENEWED: 'bg-blue-100 text-blue-700',
  PENDING: 'bg-amber-100 text-amber-700',
  UNDER_REVIEW: 'bg-blue-100 text-blue-700',
  APPROVED: 'bg-green-100 text-green-700',
  REJECTED: 'bg-red-100 text-red-700',
  PAID: 'bg-green-100 text-green-700',
  DUE: 'bg-amber-100 text-amber-700',
  OVERDUE: 'bg-red-100 text-red-700',
};

export default function StatusBadge({ status }) {
  const cls = COLORS[status] || 'bg-gray-100 text-gray-600';
  return <span className={`badge ${cls}`}>{status}</span>;
}
