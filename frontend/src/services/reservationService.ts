import { api } from './api';

export type ReservationRequest = {
    responsibleName: string;  // renamed from customerName
    responsibleEmail: string; // new field
    startTime: string;
    endTime: string;
    laps?: number;
    duration?: number;
    numPeople: number;
    monthlyVisits: number;
    birthday: boolean;
    guests: Guest[];  // new field for list of guests
};

export type Guest = {
    name: string;
    email: string;
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
    responsibleName: string;
    responsibleEmail: string;
    startTime: string;
    endTime: string;
    kartIds: string[];  // now an array of IDs instead of a single kartId
    guests: Guest[];    // list of guests
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

export const createReservation = async (reservationData: ReservationRequest): Promise<ReservationResponse> => {
    const response = await api.post('/reservations', reservationData);
    return response.data;  // Now returns a single object, not an array
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