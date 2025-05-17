import { api } from './api';

export type ReportEntry = {
    category: string;
    count: number;
    totalRevenue: number;
};

export type ReportResponse = {
    reportTitle: string;
    entries: ReportEntry[];
};

export const getRevenueByLapsReport = async (startDate: Date, endDate: Date): Promise<ReportResponse> => {
    const response = await api.get('/reports/revenue-by-laps', {
        params: {
            startDate: startDate.toISOString(),
            endDate: endDate.toISOString(),
        },
    });
    return response.data;
};

export const getRevenueByPeopleReport = async (startDate: Date, endDate: Date): Promise<ReportResponse> => {
    const response = await api.get('/reports/revenue-by-people', {
        params: {
            startDate: startDate.toISOString(),
            endDate: endDate.toISOString(),
        },
    });
    return response.data;
};