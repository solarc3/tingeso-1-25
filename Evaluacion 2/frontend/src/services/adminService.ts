import { api } from './api';

export type PriceConfig = {
    [key: string]: number;
};

const serviceMap: Record<string, string> = {
    VUELTAS: '/TARIFFS-SERVICE',
    DESCUENTO_GRUPO: '/GROUP-DISCOUNTS-SERVICE',
    DESCUENTO_FRECUENCIA: '/CUSTOMER-DISCOUNTS-SERVICE',
    DESCUENTO_CUMPLEANOS: '/SPECIAL-RATES-SERVICE'
};

function getServiceForKey(key: string): string {
    if (key.startsWith('VUELTAS')) return serviceMap.VUELTAS;
    if (key.startsWith('DESCUENTO_GRUPO')) return serviceMap.DESCUENTO_GRUPO;
    if (key.startsWith('DESCUENTO_FRECUENCIA')) return serviceMap.DESCUENTO_FRECUENCIA;
    return serviceMap.DESCUENTO_CUMPLEANOS;
}

export const getPrices = async (): Promise<PriceConfig> => {
    const urls = [
        '/TARIFFS-SERVICE/admin/prices',
        '/GROUP-DISCOUNTS-SERVICE/admin/prices',
        '/CUSTOMER-DISCOUNTS-SERVICE/admin/prices',
        '/SPECIAL-RATES-SERVICE/admin/prices'
    ];

    const responses = await Promise.all(urls.map(url => api.get(url).then(r => r.data)));
    return Object.assign({}, ...responses);
};

export const updatePrices = async (prices: PriceConfig): Promise<PriceConfig> => {
    const entries = Object.entries(prices);
    await Promise.all(entries.map(([k, v]) => updateSinglePrice(k, v)));
    return prices;
};

export const updateSinglePrice = async (
    key: string,
    price: number
): Promise<{ key: string; price: number; updated: boolean }> => {
    const service = getServiceForKey(key);
    const response = await api.post(`${service}/admin/price/${key}`, { price });
    return response.data;
};