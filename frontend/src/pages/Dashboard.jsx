import React, { useEffect, useState } from 'react';
import { Bar, Doughnut, Line } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  PointElement,
  LineElement,
  ArcElement,
  Tooltip,
  Legend,
} from 'chart.js';
import { getReportSummary, downloadMonthlyReportPdf } from '../services/reportService';

ChartJS.register(CategoryScale, LinearScale, BarElement, PointElement, LineElement, ArcElement, Tooltip, Legend);

function StatCard({ label, value, accent }) {
  return (
    <div className="card">
      <p className="text-sm text-gray-500">{label}</p>
      <p className={`text-2xl font-bold mt-1 ${accent || 'text-gray-800'}`}>{value}</p>
    </div>
  );
}

export default function Dashboard() {
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [downloading, setDownloading] = useState(false);

  useEffect(() => {
    load();
  }, []);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const { data } = await getReportSummary();
      setSummary(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load report summary');
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = async () => {
    setDownloading(true);
    try {
      await downloadMonthlyReportPdf();
    } catch (err) {
      setError('Failed to generate PDF report');
    } finally {
      setDownloading(false);
    }
  };

  if (loading) return <p className="text-gray-500">Loading dashboard...</p>;
  if (error) return <p className="text-red-600">{error}</p>;
  if (!summary) return null;

  const months = summary.monthlyBusinessReport.map((m) => m.month);

  const policyChartData = {
    labels: ['Active', 'Expired', 'Cancelled'],
    datasets: [
      {
        data: [summary.activePolicies, summary.expiredPolicies, summary.cancelledPolicies],
        backgroundColor: ['#22c55e', '#9ca3af', '#ef4444'],
      },
    ],
  };

  const claimChartData = {
    labels: ['Pending', 'Approved', 'Rejected'],
    datasets: [
      {
        label: 'Claims',
        data: [summary.pendingClaims, summary.approvedClaims, summary.rejectedClaims],
        backgroundColor: ['#f59e0b', '#22c55e', '#ef4444'],
      },
    ],
  };

  const growthChartData = {
    labels: months,
    datasets: [
      {
        label: 'New Customers',
        data: months.map((m) => summary.customerGrowthByMonth[m] ?? 0),
        borderColor: '#3366ff',
        backgroundColor: '#3366ff33',
        tension: 0.3,
      },
    ],
  };

  const premiumChartData = {
    labels: months,
    datasets: [
      {
        label: 'Premium Collected',
        data: months.map((m) => summary.premiumCollectionByMonth[m] ?? 0),
        backgroundColor: '#3366ff',
      },
    ],
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-bold text-gray-800">Reports Dashboard</h2>
        <button onClick={handleDownload} disabled={downloading} className="btn-primary">
          {downloading ? 'Generating...' : '⬇ Download Monthly PDF Report'}
        </button>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <StatCard label="Active Policies" value={summary.activePolicies} accent="text-green-600" />
        <StatCard label="Total Customers" value={summary.totalCustomers} accent="text-brand-600" />
        <StatCard label="Pending Claims" value={summary.pendingClaims} accent="text-amber-600" />
        <StatCard label="Premium Collected" value={`₹${Number(summary.totalPremiumCollected).toLocaleString()}`} accent="text-green-600" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="card">
          <h3 className="font-semibold text-gray-700 mb-4">Policy Status Breakdown</h3>
          <Doughnut data={policyChartData} />
        </div>
        <div className="card">
          <h3 className="font-semibold text-gray-700 mb-4">Claim Statistics</h3>
          <Bar data={claimChartData} options={{ plugins: { legend: { display: false } } }} />
        </div>
        <div className="card">
          <h3 className="font-semibold text-gray-700 mb-4">Customer Growth</h3>
          {months.length ? <Line data={growthChartData} /> : <p className="text-sm text-gray-400">No data yet</p>}
        </div>
        <div className="card">
          <h3 className="font-semibold text-gray-700 mb-4">Monthly Premium Collection</h3>
          {months.length ? <Bar data={premiumChartData} /> : <p className="text-sm text-gray-400">No data yet</p>}
        </div>
      </div>
    </div>
  );
}
