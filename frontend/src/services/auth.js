import axios from 'axios';
import api from './api';

// Настройка axios для работы с CORS
axios.defaults.withCredentials = false;
axios.defaults.headers.common['Content-Type'] = 'application/json';

const register = async (username, email, password) => {
  const response = await api.post('/auth/signup', {
    username,
    email,
    password
  });
  return response.data;
};

const login = async (username, password) => {
  console.log('Отправка запроса на вход:', { username });
  const response = await api.post('/auth/signin', {
    username,
    password
  });
  console.log('Получен ответ от сервера:', response.data);

  if (response.data.accessToken) {
    localStorage.setItem('user', JSON.stringify(response.data));
  }

  return response.data;
};

const logout = () => {
  localStorage.removeItem('user');
};

const getCurrentUser = () => {
  const userStr = localStorage.getItem('user');
  console.log('Получение данных пользователя из localStorage:', userStr);
  if (userStr) {
    return JSON.parse(userStr);
  }
  return null;
};

const isAuthenticated = () => {
  const user = getCurrentUser();
  console.log('Проверка аутентификации:', !!user);
  return !!user;
};

const authService = {
  register,
  login,
  logout,
  getCurrentUser,
  isAuthenticated
};

export default authService; 