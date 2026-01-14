import api from './api';

const authService = {
    login: async (email, password) => {
        const response = await api.post('/utilizadores/api/auth/login', { email, password });
        if (response.data.token) {
            localStorage.setItem('token', response.data.token);
            localStorage.setItem('user', JSON.stringify(response.data));
        }
        return response.data;
    },

    register: async (userData) => {
        return api.post('/utilizadores/api/utilizadores', userData);
    },

    logout: () => {
        logout: () => {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
        }
    }
};

export default authService;
