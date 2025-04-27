import { api } from './api';

export type ReservationRequest = {
    startTime: string;
    endTime: string;
    laps?: number;
    duration?: number;
    numPeople: number;
    monthlyVisits: number;
    birthday: boolean;
};

export type PricingResponse = {
    baseRate: number;
    groupDiscount: number;
    frequencyDiscount: number;
    birthdayDiscount: number;
    tax: number;
    totalAmount: number;
};

export type ReservationResponse = {
    id: number;
    startTime: string;
    endTime: string;
    kartId: string;
    numPeople: number;
    baseRate: number;
    groupDiscount: number;
    frequencyDiscount: number;
    birthdayDiscount: number;
    tax: number;
    totalAmount: number;
    status: string;
};

export const checkPricing = async (reservationData: ReservationRequest): Promise<PricingResponse> => {
    const response = await api.post('/reservations/check', reservationData);
    return response.data;
};

export const createReservation = async (reservationData: ReservationRequest): Promise<ReservationResponse[]> => {
    const response = await api.post('/reservations', reservationData);
    return response.data;
};

export const getReservations = async (startDate: Date, endDate: Date): Promise<ReservationResponse[]> => {
    const response = await api.get('/reservations', {
        params: {
            startDate: startDate.toISOString(),
            endDate: endDate.toISOString(),
        },
    });
    return response.data;
};