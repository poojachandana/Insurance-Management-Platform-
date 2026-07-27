import React, { useEffect, useState } from 'react';
import { getMyPolicies } from '../services/policyService';
import StatusBadge from '../components/StatusBadge';

export default function MyPolicies() {
  const [policies, setPolicies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    (async () => {
      try {
        const { data } = await getMyPolicies();
        setPolicies(data);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load your policies');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  return (
    <div className="space-y-6">
      <h2 className="text-xl font-bold text-gray-800">My Policies</h2>
      {error && <div className="text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">{error}</div>}

      {loading ? (
        <p className="text-gray-500">Loading...</p>
      ) : policies.length === 0 ? (
        <div className="card text-gray-500">You don't have any policies yet. Contact an agent to set one up.</div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {policies.map((p) => (
            <div key={p.id} className="card">
              <div className="flex justify-between items-start mb-3">
                <div>
                  <p className="text-xs text-gray-400">{p.policyNumber}</p>
                  <p className="font-bold text-gray-800">{p.policyType}</p>
                </div>
                <StatusBadge status={p.status} />
              </div>
              <div className="grid grid-cols-2 gap-2 text-sm text-gray-600">
                <p><span className="text-gray-400">Premium:</span> ₹{Number(p.premiumAmount).toLocaleString()}</p>
                <p><span className="text-gray-400">Start:</span> {p.startDate}</p>
                <p><span className="text-gray-400">End:</span> {p.endDate}</p>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
