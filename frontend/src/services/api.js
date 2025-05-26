import axios from 'axios';
import authService from './auth';

const API_URL = '/api';

// axios с базовыми настройками
const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

// перехватчик запросов
api.interceptors.request.use(
  (config) => {
    console.log('Подготовка запроса:', config);
    const user = authService.getCurrentUser();
    console.log('Текущий пользователь:', user);
    if (user && user.accessToken) {
      config.headers['Authorization'] = `Bearer ${user.accessToken}`;
      console.log('Установлен токен в заголовок:', config.headers['Authorization']);
    } else {
      console.log('Токен не найден в localStorage');
    }
    return config;
  },
  (error) => {
    console.error('Ошибка при подготовке запроса:', error);
    return Promise.reject(error);
  }
);

// перехватчик ответов
api.interceptors.response.use(
  (response) => {
    console.log('Получен ответ:', response);
    return response;
  },
  (error) => {
    console.error('Ошибка ответа:', error.response);
    console.error('Детали ошибки:', {
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data,
      headers: error.response?.headers
    });
    
    // ошибка (только 401)
    if (error.response?.status === 401) {
      console.log('Обнаружена ошибка авторизации (401)');
      localStorage.removeItem('user');
      return Promise.reject(new Error('Требуется авторизация для доступа к ресурсу'));
    }
    
    // Для ошибки 403 просто возвращаем ошибку
    if (error.response?.status === 403) {
      console.log('Обнаружена ошибка доступа (403)');
      return Promise.reject(new Error('Нет доступа к ресурсу'));
    }
    
    return Promise.reject(error);
  }
);

const getMovies = async () => {
  try {
    console.log('Отправка запроса на получение фильмов');
    const response = await api.get('/movies');
    return response.data;
  } catch (error) {
    console.error('Error fetching movies:', error);
    throw error;
  }
};

const getMovieById = async (tmdbId) => {
  try {
    console.log('Запрос фильма по tmdbId:', tmdbId);
    const response = await api.get(`/movies/${tmdbId}`);
    console.log('Ответ сервера:', response.data);
    return response.data;
  } catch (error) {
    console.error(`Error fetching movie ${tmdbId}:`, error);
    throw error;
  }
};

const getMovieRecommendations = async (id) => {
  try {
    const response = await api.get(`/movies/recommendations`);
    const filteredRecommendations = response.data.filter(movie => movie.tmdbId != id);
    console.log('Отфильтрованные рекомендации:', filteredRecommendations);
    return filteredRecommendations;
  } catch (error) {
    console.error(`Error fetching recommendations for movie ${id}:`, error);
    throw error;
  }
};

const getUserRecommendations = async (id) => {
  try {
    const response = await api.get(`/recommendations/user/${id}`);
    return response.data;
  } catch (error) {
    if (error.response && error.response.status === 403) {
      console.error('Ошибка доступа к рекомендациям:', error);
      throw new Error('Требуется авторизация для получения рекомендаций');
    }
    console.error('Ошибка при получении рекомендаций:', error);
    throw error;
  }
};

const getRatedMovies = async (id) => {
  try {
    const response = await api.get(`/ratings/user/${id}`);
    return response.data;
  } catch (error) {
    console.error('Ошибка при получении оцененных фильмов:', error);
    throw error;
  }
};

const submitRating = async (movieId, score) => {
  try {
    console.log('Отправка оценки:', { movieId, score });
    const response = await api.post(`/ratings/${movieId}?rating=${score}`);
    console.log('Ответ на отправку оценки:', response);
    return response.data;
  } catch (error) {
    console.error('Error submitting rating:', error);
    throw error;
  }
};

const getWatchlist = async () => {
  try {
    const response = await api.get('/watchlist');
    return response.data;
  } catch (error) {
    console.error('Ошибка при получении списка отмеченных фильмов:', error);
    throw error;
  }
};

// Экспортируем api и методы для работы с фильмами
export default {
  ...api,
  getMovies,
  getMovieById,
  getMovieRecommendations,
  getUserRecommendations,
  getRatedMovies,
  submitRating,
  getWatchlist
}; 