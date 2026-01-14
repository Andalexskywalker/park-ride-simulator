import api from './api';

const parkService = {
    getParks: async () => {
        const response = await api.get('/parques/api/parques');
        return response.data;
    },

    getParkById: async (id) => {
        const response = await api.get(`/parques/api/parques/${id}`);
        return response.data;
    }
};

export default parkService;
