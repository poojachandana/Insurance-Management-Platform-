import React, { useEffect, useState } from 'react';
import { getAuditLogs } from '../services/auditLogService';
import Pagination from '../components/Pagination';

const PAGE_SIZE = 20;
const ENTITY_TYPES = ['ALL', 'CUSTOMER', 'POLICY', 'CLAIM', 'PREMIUM', 'DOCUMENT', 'EMPLOYEE', 'SETTINGS', 'POLICY_CATEGORY', 'ACCOUNT'];

const ACTION_COLORS = {
  CREATE: 'bg-green-100 text-green-700',
  UPDATE: 'bg-blue-100 text-blue-700',
  DELETE: 'bg-red-100 text-red-700',
  CANCEL: 'bg-red-100 text-red-700',
  RENEW: 'bg-blue-100 text-blue-700',
  APPROVED: 'bg-green-100 text-green-700',
  REJECTED: 'bg-red-100 text-red-700',
  ASSIGN: 'bg-purple-100 text-purple-700',
  PAY: 'bg-green-100 text-green-700',
  SUBMIT: 'bg-amber-100 text-amber-700',
  AUTO_EXPIRE: 'bg-gray-200 text-gray-600',
  AUTO_OVERDUE: 'bg-gray-200 text-gray-600',
};

export default function AuditLogs() {
  const [pageData, setPageData] = useState({ content: [], page: 0, totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [entityType, setEntityType] = useState('ALL');
  const [actorInput, setActorInput] = useState('');
  const [actorEmail, setActorEmail] = useState('');
  const [page, setPage] = useState(0);

  useEffect(() => {
    load();
  }, [page, entityType, actorEmail]);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const { data } = await getAuditLogs({
        page,
        size: PAGE_SIZE,
        entityType: entityType === 'ALL' ? undefined : entityType,
        actorEmail: actorEmail || undefined,
      });
      setPageData(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load audit logs');
    } finally {
      setLoading(false);
    }
  };

  const handleFilterSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    setActorEmail(actorInput);
  };

  const handleEntityChange = (t) => {
    setEntityType(t);
    setPage(0);
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-xl font-bold text-gray-800">Audit Logs</h2>
        <p className="text-sm text-gray-500">A trail of significant actions taken across the system</p>
      </div>

      <div className="flex flex-wrap items-center gap-3">
        <form onSubmit={handleFilterSubmit} className="flex gap-2">
          <input
            value={actorInput}
            onChange={(e) => setActorInput(e.target.value)}
            placeholder="Filter by actor email"
            className="input-field w-64"
          />
          <button type="submit" className="btn-secondary">Filter</button>
        </form>
        <div className="flex gap-2 flex-wrap">
          {ENTITY_TYPES.map((t) => (
            <button
              key={t}
              onClick={() => handleEntityChange(t)}
              className={`text-xs font-medium px-3 py-1.5 rounded-full transition-colors ${
                entityType === t ? 'bg-brand-500 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              {t}
            </button>
          ))}
        </div>
      </div>

      {error && <div className="text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">{error}</div>}

      <div className="card overflow-x-auto">
        {loading ? (
          <p className="text-gray-500">Loading audit logs...</p>
        ) : pageData.content.length === 0 ? (
          <p className="text-gray-500">No audit log entries found.</p>
        ) : (
          <>
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-gray-500 border-b">
                  <th className="py-2 pr-4">Timestamp</th>
                  <th className="py-2 pr-4">Actor</th>
                  <th className="py-2 pr-4">Action</th>
                  <th className="py-2 pr-4">Entity</th>
                  <th className="py-2 pr-4">Details</th>
                </tr>
              </thead>
              <tbody>
                {pageData.content.map((log) => (
                  <tr key={log.id} className="border-b last:border-0 hover:bg-gray-50">
                    <td className="py-3 pr-4 text-gray-500 whitespace-nowrap">{new Date(log.timestamp).toLocaleString()}</td>
                    <td className="py-3 pr-4 text-gray-700">{log.actorEmail}</td>
                    <td className="py-3 pr-4">
                      <span className={`badge ${ACTION_COLORS[log.action] || 'bg-gray-100 text-gray-600'}`}>{log.action}</span>
                    </td>
                    <td className="py-3 pr-4 text-gray-600">
                      {log.entityType}{log.entityId ? ` #${log.entityId}` : ''}
                    </td>
                    <td className="py-3 pr-4 text-gray-600 max-w-md">{log.details}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            <Pagination page={pageData.page} totalPages={pageData.totalPages} totalElements={pageData.totalElements} onPageChange={setPage} />
          </>
        )}
      </div>
    </div>
  );
}
