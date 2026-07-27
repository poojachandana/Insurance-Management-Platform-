import React, { useEffect, useState } from 'react';
import { getMyPremiums, payPremium } from '../services/premiumService';
import StatusBadge from '../components/StatusBadge';

export default function MyPremiums() {
  const [premiums, setPremiums] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    load();
  }, []);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const { data } = await getMyPremiums();
      setPremiums(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load your premium payments');
    } finally {
      setLoading(false);
    }
  };

  const handlePay = async (id) => {
    try {
      await payPremium(id);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to record payment');
    }
  };

  return (
    <div className="space-y-6">
      <h2 className="text-xl font-bold text-gray-800">My Premium Payments</h2>
      {error && <div className="text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">{error}</div>}

      <div className="card overflow-x-auto">
        {loading ? (
          <p className="text-gray-500">Loading...</p>
        ) : premiums.length === 0 ? (
          <p className="text-gray-500">No premium payments scheduled yet.</p>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-gray-500 border-b">
                <th className="py-2 pr-4">Policy #</th>
                <th className="py-2 pr-4">Amount</th>
                <th className="py-2 pr-4">Due Date</th>
                <th className="py-2 pr-4">Paid On</th>
                <th className="py-2 pr-4">Status</th>
                <th className="py-2 pr-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {premiums.map((p) => (
                <tr key={p.id} className="border-b last:border-0">
                  <td className="py-3 pr-4 font-medium text-gray-800">{p.policyNumber}</td>
                  <td className="py-3 pr-4 text-gray-600">₹{Number(p.amount).toLocaleString()}</td>
                  <td className="py-3 pr-4 text-gray-500">{p.dueDate}</td>
                  <td className="py-3 pr-4 text-gray-500">{p.paymentDate || '—'}</td>
                  <td className="py-3 pr-4"><StatusBadge status={p.paymentStatus} /></td>
                  <td className="py-3 pr-4 text-right">
                    {p.paymentStatus !== 'PAID' && (
                      <button onClick={() => handlePay(p.id)} className="btn-primary !px-3 !py-1.5 text-xs">Pay Now</button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
