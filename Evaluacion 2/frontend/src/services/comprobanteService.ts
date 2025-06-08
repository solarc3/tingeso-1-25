import { api } from './api';

export type ComprobanteResponse = {
    id: number;
    codigoReserva: string;
    fechaEmision: string;
    enviado: boolean;
    reservaId: number;
};

// Obtener información del comprobante
export const getComprobante = async (id: number): Promise<ComprobanteResponse> => {
    const response = await api.get(`/RESERVATIONS-SERVICE//${id}`);
    return response.data;
};

// Enviar el comprobante por email
export const enviarComprobante = async (id: number): Promise<string> => {
    const response = await api.post(`/RESERVATIONS-SERVICE//enviar/${id}`);
    return response.data;
};

// Descargar el PDF del comprobante
export const descargarComprobantePdf = async (id: number): Promise<void> => {
    try {
        const response = await api.get(`/RESERVATIONS-SERVICE//${id}/pdf`, {
            responseType: 'blob'
        });

        // Crear URL para el blob y forzar la descarga
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', `comprobante-${id}.pdf`);
        document.body.appendChild(link);
        link.click();

        // Limpiar
        window.URL.revokeObjectURL(url);
        link.remove();
    } catch (error) {
        console.error('Error descargando comprobante:', error);
        throw error;
    }
};
export const getComprobanteByReserva = async (reservaId: number): Promise<ComprobanteResponse> => {
    const response = await api.get(`/RESERVATIONS-SERVICE//reserva/${reservaId}`);
    return response.data;
};
