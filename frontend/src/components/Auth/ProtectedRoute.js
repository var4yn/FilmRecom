import React from 'react';
import { Navigate } from 'react-router-dom';
import authService from '../../services/auth';

function ProtectedRoute({ children }) {
  if (!authService.isAuthenticated()) {
    return <Navigate to="/login" />;
  }

  return children;
}

export default ProtectedRoute; 