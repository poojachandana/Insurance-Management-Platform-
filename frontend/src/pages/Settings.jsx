import React, { useEffect, useState } from 'react';
import { getSettings, updateSettings } from '../services/settingsService';

export default function Settings() {
  const [form, setForm] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        const { data } = await getSettings();
        setForm(data);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load settings');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const handleChange = (e) => {
    const { name, value, type } = e.target;
    setForm({ ...form, [name]: type === 'number' ? Number(value) : value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    setSaved(false);
    try {
      const { data } = await updateSettings(form);
      setForm(data);
      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update settings');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <p className="text-gray-500">Loading settings...</p>;
  if (!form) return null;

  return (
    <div className="space-y-6 max-w-2xl">
      <div>
        <h2 className="text-xl font-bold text-gray-800">System Settings</h2>
        <p className="text-sm text-gray-500">Company details and automated reminder configuration</p>
      </div>

      {error && <div className="text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">{error}</div>}
      {saved && <div className="text-sm text-green-700 bg-green-50 border border-green-100 rounded-lg px-3 py-2">Settings saved successfully.</div>}

      <form onSubmit={handleSubmit} className="card space-y-5">
        <div>
          <h3 className="font-semibold text-gray-700 mb-3">Company Details</h3>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Company name</label>
              <input name="companyName" required value={form.companyName} onChange={handleChange} className="input-field" />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Support email</label>
                <input type="email" name="supportEmail" value={form.supportEmail || ''} onChange={handleChange} className="input-field" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Support phone</label>
                <input name="supportPhone" value={form.supportPhone || ''} onChange={handleChange} className="input-field" />
              </div>
            </div>
          </div>
        </div>

        <div className="border-t pt-5">
          <h3 className="font-semibold text-gray-700 mb-3">Automation Rules</h3>
          <div className="grid grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Default policy term (months)</label>
              <input type="number" min="1" name="defaultPolicyTermMonths" required value={form.defaultPolicyTermMonths} onChange={handleChange} className="input-field" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Expiry reminder (days before)</label>
              <input type="number" min="1" name="policyExpiryReminderDays" required value={form.policyExpiryReminderDays} onChange={handleChange} className="input-field" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Premium grace period (days)</label>
              <input type="number" min="0" name="premiumGraceDays" required value={form.premiumGraceDays} onChange={handleChange} className="input-field" />
            </div>
          </div>
          <p className="text-xs text-gray-400 mt-2">
            These values control the daily background jobs: how many days before a policy's end date customers/agents
            get an expiry reminder, and how many days after a premium's due date it's marked overdue.
          </p>
        </div>

        <div className="flex justify-end pt-2">
          <button type="submit" disabled={saving} className="btn-primary">{saving ? 'Saving...' : 'Save Settings'}</button>
        </div>
      </form>
    </div>
  );
}
