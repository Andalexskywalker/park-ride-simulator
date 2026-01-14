import api from './api';

const sessionService = {
    startSession: async (matricula, parqueId) => {
        return api.post('/sessoes/api/sessoes/start', { matricula, parqueId });
    },

    stopSession: async (id) => {
        return api.post(`/sessoes/api/sessoes/${id}/stop`);
    },

    getActiveSession: async (matricula) => {
        return api.get(`/sessoes/api/sessoes/active/${matricula}`);
    },

    getAnalytics: async () => {
        return api.get('/sessoes/api/sessoes/analytics');
    },

    getInvoices: async (matricula) => {
        // Tenta rota padrao 'tarifas'. Se falhar, o frontend pode tentar outra, mas aqui hardcode
        return api.get(`/tarifas/api/faturas/matricula/${matricula}`);
    },

    getActiveByParque: async (parqueId) => {
        return api.get(`/sessoes/api/sessoes/active/parque/${parqueId}`);
    }
};

export default sessionService;
