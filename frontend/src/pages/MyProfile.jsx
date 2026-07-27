import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { getMyAccount, updateMyAccount, changeMyPassword } from '../services/accountService';
import { getMyProfile, updateMyProfile } from '../services/customerService';

export default function MyProfile() {
  const { user, logout } = useAuth();
  const isCustomer = user?.role === 'CUSTOMER';

  const [account, setAccount] = useState(null);
  const [customer, setCustomer] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [accountForm, setAccountForm] = useState({ name: '', email: '' });
  const [savingAccount, setSavingAccount] = useState(false);

  const [customerForm, setCustomerForm] = useState({ name: '', phone: '', address: '', dob: '' });
  const [savingCustomer, setSavingCustomer] = useState(false);

  const [passwordForm, setPasswordForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [savingPassword, setSavingPassword] = useState(false);

  useEffect(() => {
    init();
  }, []);

  const init = async () => {
    setLoading(true);
    setError('');
    try {
      const { data: acc } = await getMyAccount();
      setAccount(acc);
      setAccountForm({ name: acc.name, email: acc.email });

      if (isCustomer) {
        const { data: cust } = await getMyProfile();
        setCustomer(cust);
        setCustomerForm({
          name: cust.name,
          phone: cust.phone || '',
          address: cust.address || '',
          dob: cust.dob || '',
        });
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load profile');
    } finally {
      setLoading(false);
    }
  };

  const flashSuccess = (msg) => {
    setSuccess(msg);
    setTimeout(() => setSuccess(''), 3000);
  };

  const handleAccountSubmit = async (e) => {
    e.preventDefault();
    setSavingAccount(true);
    setError('');
    try {
      const { data } = await updateMyAccount(accountForm);
      setAccount(data);
      if (data.newToken) {
        // Email changed — the JWT subject is the email, so refresh the stored token
        // to keep the session valid without forcing a logout.
        localStorage.setItem('token', data.newToken);
      }
      flashSuccess('Profile updated successfully.');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update profile');
    } finally {
      setSavingAccount(false);
    }
  };

  const handleCustomerSubmit = async (e) => {
    e.preventDefault();
    setSavingCustomer(true);
    setError('');
    try {
      const { data } = await updateMyProfile({ ...customerForm, dob: customerForm.dob || null });
      setCustomer(data);
      flashSuccess('Contact details updated successfully.');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update contact details');
    } finally {
      setSavingCustomer(false);
    }
  };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setError("New password and confirmation don't match");
      return;
    }
    setSavingPassword(true);
    try {
      await changeMyPassword({
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword,
      });
      setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
      flashSuccess('Password changed successfully.');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to change password');
    } finally {
      setSavingPassword(false);
    }
  };

  if (loading) return <p className="text-gray-500">Loading profile...</p>;

  return (
    <div className="space-y-6 max-w-2xl">
      <div>
        <h2 className="text-xl font-bold text-gray-800">My Profile</h2>
        <p className="text-sm text-gray-500">Manage your account details and password</p>
      </div>

      {error && <div className="text-sm text-red-600 bg-red-50 border border-red-100 rounded-lg px-3 py-2">{error}</div>}
      {success && <div className="text-sm text-green-700 bg-green-50 border border-green-100 rounded-lg px-3 py-2">{success}</div>}

      <form onSubmit={handleAccountSubmit} className="card space-y-4">
        <h3 className="font-semibold text-gray-700">Account Details</h3>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Full name</label>
          <input
            value={accountForm.name}
            onChange={(e) => setAccountForm({ ...accountForm, name: e.target.value })}
            required
            className="input-field"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Email (also your login ID)</label>
          <input
            type="email"
            value={accountForm.email}
            onChange={(e) => setAccountForm({ ...accountForm, email: e.target.value })}
            required
            className="input-field"
          />
        </div>
        <div className="flex items-center justify-between">
          <span className="badge bg-brand-100 text-brand-700">{account?.role}</span>
          <button type="submit" disabled={savingAccount} className="btn-primary">{savingAccount ? 'Saving...' : 'Save Account Details'}</button>
        </div>
      </form>

      {isCustomer && customer && (
        <form onSubmit={handleCustomerSubmit} className="card space-y-4">
          <h3 className="font-semibold text-gray-700">Contact Details</h3>
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">Phone</label>
              <input
                value={customerForm.phone}
                onChange={(e) => setCustomerForm({ ...customerForm, phone: e.target.value })}
                className="input-field"
              />
            </div>
            <div className="col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">Address</label>
              <input
                value={customerForm.address}
                onChange={(e) => setCustomerForm({ ...customerForm, address: e.target.value })}
                className="input-field"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Date of birth</label>
              <input
                type="date"
                value={customerForm.dob}
                onChange={(e) => setCustomerForm({ ...customerForm, dob: e.target.value })}
                className="input-field"
              />
            </div>
          </div>
          <div className="flex justify-end">
            <button type="submit" disabled={savingCustomer} className="btn-primary">{savingCustomer ? 'Saving...' : 'Save Contact Details'}</button>
          </div>
        </form>
      )}

      <form onSubmit={handlePasswordSubmit} className="card space-y-4" autoComplete="off">
        <h3 className="font-semibold text-gray-700">Change Password</h3>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Current password</label>
          <input
              type="password"
              required
              autoComplete="current-password"
              value={passwordForm.currentPassword}
              onChange={(e) => setPasswordForm({ ...passwordForm, currentPassword: e.target.value })}
              className="input-field"
          />
        </div>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">New password</label>
            <PasswordInput
                required
                autoComplete="current-password"
                value={passwordForm.currentPassword}
                onChange={(e) => setPasswordForm({ ...passwordForm, currentPassword: e.target.value })}
                className="input-field"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Confirm new password</label>
            <input
                type="password"
                required
                minLength={6}
                autoComplete="new-password"
                value={passwordForm.confirmPassword}
                onChange={(e) => setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })}
                className="input-field"
            />
          </div>
        </div>
        <div className="flex justify-end">
          <button type="submit" disabled={savingPassword} className="btn-primary">{savingPassword ? 'Saving...' : 'Change Password'}</button>
        </div>
      </form>
    </div>
  );
}
