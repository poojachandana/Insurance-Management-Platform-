import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import ProtectedRoute from './components/ProtectedRoute';
import DashboardLayout from './layouts/DashboardLayout';

import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Customers from './pages/Customers';
import Policies from './pages/Policies';
import Claims from './pages/Claims';
import Premiums from './pages/Premiums';
import Documents from './pages/Documents';
import Employees from './pages/Employees';
import Settings from './pages/Settings';
import AuditLogs from './pages/AuditLogs';
import PolicyCategories from './pages/PolicyCategories';
import MyProfile from './pages/MyProfile';
import MyPolicies from './pages/MyPolicies';
import MyClaims from './pages/MyClaims';
import MyPremiums from './pages/MyPremiums';
import MyDocuments from './pages/MyDocuments';
import NotFound from './pages/NotFound';

function RootRedirect() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  return <Navigate to={user.role === 'CUSTOMER' ? '/my-policies' : '/dashboard'} replace />;
}

export default function App() {
  return (
    <ThemeProvider>
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          <Route element={
            <ProtectedRoute>
              <DashboardLayout />
            </ProtectedRoute>
          }>
            {/* Admin / Agent routes */}
            <Route path="/dashboard" element={
              <ProtectedRoute roles={['ADMIN', 'AGENT']}><Dashboard /></ProtectedRoute>
            } />
            <Route path="/customers" element={
              <ProtectedRoute roles={['ADMIN', 'AGENT']}><Customers /></ProtectedRoute>
            } />
            <Route path="/policies" element={
              <ProtectedRoute roles={['ADMIN', 'AGENT']}><Policies /></ProtectedRoute>
            } />
            <Route path="/claims" element={
              <ProtectedRoute roles={['ADMIN', 'AGENT']}><Claims /></ProtectedRoute>
            } />
            <Route path="/premiums" element={
              <ProtectedRoute roles={['ADMIN', 'AGENT']}><Premiums /></ProtectedRoute>
            } />
            <Route path="/documents" element={
              <ProtectedRoute roles={['ADMIN', 'AGENT']}><Documents /></ProtectedRoute>
            } />
            <Route path="/employees" element={
              <ProtectedRoute roles={['ADMIN']}><Employees /></ProtectedRoute>
            } />
            <Route path="/settings" element={
              <ProtectedRoute roles={['ADMIN']}><Settings /></ProtectedRoute>
            } />
            <Route path="/audit-logs" element={
              <ProtectedRoute roles={['ADMIN']}><AuditLogs /></ProtectedRoute>
            } />
            <Route path="/policy-categories" element={
              <ProtectedRoute roles={['ADMIN']}><PolicyCategories /></ProtectedRoute>
            } />

            {/* Available to every logged-in role */}
            <Route path="/profile" element={<MyProfile />} />

            {/* Customer routes */}
            <Route path="/my-policies" element={
              <ProtectedRoute roles={['CUSTOMER']}><MyPolicies /></ProtectedRoute>
            } />
            <Route path="/my-claims" element={
              <ProtectedRoute roles={['CUSTOMER']}><MyClaims /></ProtectedRoute>
            } />
            <Route path="/my-premiums" element={
              <ProtectedRoute roles={['CUSTOMER']}><MyPremiums /></ProtectedRoute>
            } />
            <Route path="/my-documents" element={
              <ProtectedRoute roles={['CUSTOMER']}><MyDocuments /></ProtectedRoute>
            } />
          </Route>

          <Route path="/" element={
            <ProtectedRoute><RootRedirect /></ProtectedRoute>
          } />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
    </ThemeProvider>
  );
}
