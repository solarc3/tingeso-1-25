import { api } from './api';

export type PriceConfig = {
    [key: string]: number;
};

export const getPrices = async (): Promise<PriceConfig> => {
    const response = await api.get('/admin/prices');
    return response.data;
};

export const updatePrices = async (prices: PriceConfig): Promise<PriceConfig> => {
    const response = await api.post('/admin/prices', { prices });
    return response.data;
};

export const updateSinglePrice = async (key: string, price: number): Promise<{key: string, price: number, updated: boolean}> => {
    const response = await api.post(`/admin/price/${key}`, { price });
    return response.data;
};