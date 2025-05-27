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

export type KartAvailabilityResponse = {
    time: string;
    totalKarts: number;
    availableKarts: number;
};

export const checkPricing = async (reservationData: ReservationRequest): Promise<PricingResponse> => {
    const data = {
        ...reservationData,
        startTime: ensureTimezone(reservationData.startTime),
        endTime: ensureTimezone(reservationData.endTime),
    };

    const response = await api.post('/reservations/check', data);
    return response.data;
};

export const createReservation = async (reservationData: ReservationRequest): Promise<ReservationResponse> => {
    const data = {
        ...reservationData,
        startTime: ensureTimezone(reservationData.startTime),
        endTime: ensureTimezone(reservationData.endTime),
    };

    const response = await api.post('/reservations', data);
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

function ensureTimezone(dateString: string): string {
    const date = new Date(dateString);
    if (!dateString.endsWith('Z') && !dateString.includes('+')) {
        return date.toISOString(); // Convert to ISO with timezone
    }
    return dateString;
}

export const checkKartAvailability = async (startTime: string, endTime: string): Promise<KartAvailabilityResponse> => {
    const response = await api.get('/reservations/availability', {
        params: {
            startTime,
            endTime
        }
    });
    return response.data;
};

export const cancelReservation = async (id: number): Promise<ReservationResponse> => {
    const response = await api.post(`/reservations/${id}/cancel`);
    return response.data;
};