import api from './api';

const utilizadorService = {
    getMe: async () => {
        // In a real app we'd have a /me endpoint, 
        // for now we'll fetch the user info manually or assume it's in the state
        // Let's assume we fetch by ID or get all and filter (less efficient but works for now)
        const response = await api.get('/utilizadores/api/utilizadores');
        return response.data;
    },

    getViaturas: async (userId) => {
        // We get the user details which includes the list of vehicles
        const response = await api.get(`/utilizadores/api/utilizadores/${userId}`);
        return response.data.viaturas || [];
    },

    addViatura: async (userId, viaturaData) => {
        const response = await api.post(`/utilizadores/api/utilizadores/${userId}/viaturas`, viaturaData);
        return response.data;
    },

    searchByMatricula: async (matricula) => {
        return api.get(`/utilizadores/api/utilizadores/search/matricula/${matricula}`);
    },

    createOperator: async (userData) => {
        return api.post('/utilizadores/api/utilizadores/operator', userData);
    },

    getAll: async () => {
        return api.get('/utilizadores/api/utilizadores');
    }
};

export default utilizadorService;
