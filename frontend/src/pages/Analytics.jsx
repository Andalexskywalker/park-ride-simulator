import React, { useState, useEffect } from 'react';
import sessionService from '../services/sessionService';
import { BarChart3, TrendingUp, Users, DollarSign, ArrowUpRight, Clock, MapPin, Database } from 'lucide-react';

const Analytics = () => {
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchAnalytics = async () => {
            try {
                const res = await sessionService.getAnalytics();
                // Map backend keys to frontend keys
                setData({
                    faturacaoTotal: res.data.receitaTotal,
                    totalSessoesFaturadas: res.data.totalSessoes,
                    activeSessions: res.data.sessoesAtivas
                });
            } catch (err) {
                console.error('Erro ao carregar analytics', err);
            } finally {
                setLoading(false);
            }
        };
        fetchAnalytics();
    }, []);

    const hasData = data && data.totalSessoesFaturadas > 0;

    if (loading) return (
        <div className="analytics-loading glass">
            <div className="loader"></div>
            <p>A compilar métricas do sistema...</p>
        </div>
    );

    return (
        <div className="analytics-view fadeIn">
            <header className="main-header">
                <div>
                    <h1>Relatório de Performance</h1>
                    <p>Visão geral da operação global dos parques P+R</p>
                </div>
                {hasData && <div className="period-badge glass">Atualizado em Tempo Real</div>}
            </header>

            <div className="analytics-grid">
                <div className="metric-card glass highlight">
                    <div className="metric-header">
                        <div className="icon-box purple"><DollarSign size={20} /></div>
                        {hasData && <span className="trend positive"><ArrowUpRight size={14} /> 0.0%</span>}
                    </div>
                    <div className="metric-info">
                        <span>Faturação Total</span>
                        <h3>{data?.faturacaoTotal || 0} €</h3>
                        <p>Receita bruta acumulada</p>
                    </div>
                </div>

                <div className="metric-card glass">
                    <div className="metric-header">
                        <div className="icon-box blue"><BarChart3 size={20} /></div>
                        {hasData && <span className="trend positive"><ArrowUpRight size={14} /> 0%</span>}
                    </div>
                    <div className="metric-info">
                        <span>Sessões Concluídas</span>
                        <h3>{data?.totalSessoesFaturadas || 0}</h3>
                        <p>Utilizações pagas</p>
                    </div>
                </div>

                <div className="metric-card glass">
                    <div className="metric-header">
                        <div className="icon-box green"><Clock size={20} /></div>
                    </div>
                    <div className="metric-info">
                        <span>Tempo Médio</span>
                        <h3>{hasData ? "0h 45m" : "0h 00m"}</h3>
                        <p>Duração por sessão</p>
                    </div>
                </div>

                <div className="metric-card glass">
                    <div className="metric-header">
                        <div className="icon-box orange"><MapPin size={20} /></div>
                    </div>
                    <div className="metric-info">
                        <span>Parque Mais Ativo</span>
                        <h3>{hasData ? "Ponte G. Vasco" : "N/A"}</h3>
                        <p>Localização principal</p>
                    </div>
                </div>
            </div>

            <div className="charts-row">
                <div className="chart-container glass">
                    <div className="chart-header">
                        <h3>Ocupação vs Faturação</h3>
                        <p>Distribuição de tráfego e receita</p>
                    </div>

                    {!hasData ? (
                        <div className="empty-chart-state">
                            <Database size={48} />
                            <h4>Sem dados suficientes</h4>
                            <p>As métricas aparecerão assim que as primeiras sessões forem faturadas no sistema.</p>
                        </div>
                    ) : (
                        <div className="mock-chart main-chart">
                            {[10, 20, 15, 30, 25, 40, 35, 50, 45, 60, 55, 70].map((h, i) => (
                                <div key={i} className="bar-group">
                                    <div className="bar primary" style={{ height: `${h}%` }}></div>
                                    <div className="bar secondary" style={{ height: `${h * 0.7}%` }}></div>
                                    <span className="bar-label">{i + 1}h</span>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>

            <style>{`
                .analytics-view {
                    animation: fadeIn 0.6s cubic-bezier(0.4, 0, 0.2, 1);
                }
                .main-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: flex-start;
                    margin-bottom: 35px;
                }
                .period-badge {
                    padding: 8px 16px;
                    border-radius: 20px;
                    font-size: 0.8rem;
                    color: var(--secondary);
                    background: rgba(0, 229, 255, 0.1);
                }
                .analytics-grid {
                    display: grid;
                    grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
                    gap: 20px;
                    margin-bottom: 30px;
                }
                .metric-card {
                    padding: 24px;
                    display: flex;
                    flex-direction: column;
                    gap: 20px;
                    transition: transform 0.3s;
                }
                .metric-card:hover { transform: translateY(-5px); }
                .metric-card.highlight { border-color: rgba(124, 77, 255, 0.3); }
                
                .metric-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                }
                .trend {
                    display: flex;
                    align-items: center;
                    gap: 4px;
                    font-size: 0.75rem;
                    font-weight: 600;
                    padding: 4px 8px;
                    border-radius: 12px;
                }
                .trend.positive { background: rgba(0, 230, 118, 0.1); color: #00e676; }
                
                .icon-box {
                    width: 44px;
                    height: 44px;
                    border-radius: 12px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                }
                .purple { background: rgba(124, 77, 255, 0.15); color: #7c4dff; }
                .blue { background: rgba(0, 229, 255, 0.15); color: #00e5ff; }
                .green { background: rgba(0, 230, 118, 0.15); color: #00e676; }
                .orange { background: rgba(255, 171, 0, 0.15); color: #ffab00; }
                
                .metric-info span { font-size: 0.85rem; color: var(--text-muted); }
                .metric-info h3 { font-size: 2rem; font-weight: 700; margin: 8px 0; color: white; }
                .metric-info p { font-size: 0.75rem; color: var(--text-muted); }

                .chart-container { padding: 35px; min-height: 400px; display: flex; flex-direction: column; }
                .chart-header { margin-bottom: 40px; }
                .chart-header h3 { font-size: 1.25rem; }
                .chart-header p { font-size: 0.85rem; color: var(--text-muted); }
                
                .empty-chart-state {
                    flex: 1;
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    justify-content: center;
                    color: var(--text-muted);
                    text-align: center;
                    gap: 15px;
                }
                .empty-chart-state svg { opacity: 0.2; }
                .empty-chart-state h4 { color: white; }

                .main-chart {
                    height: 250px;
                    display: flex;
                    align-items: flex-end;
                    justify-content: space-between;
                    gap: 10px;
                    padding-bottom: 30px;
                }
                .bar-group {
                    flex: 1;
                    height: 100%;
                    display: flex;
                    align-items: flex-end;
                    gap: 4px;
                    position: relative;
                }
                .bar {
                    width: 100%;
                    border-radius: 4px 4px 2px 2px;
                    background: var(--primary);
                }
                .bar.secondary { opacity: 0.4; background: var(--secondary); }
                .bar-label {
                    position: absolute;
                    bottom: -25px;
                    left: 50%;
                    transform: translateX(-50%);
                    font-size: 0.7rem;
                    color: var(--text-muted);
                }

                @keyframes fadeIn {
                    from { opacity: 0; transform: translateY(20px); }
                    to { opacity: 1; transform: translateY(0); }
                }
            `}</style>
        </div>
    );
};

export default Analytics;
