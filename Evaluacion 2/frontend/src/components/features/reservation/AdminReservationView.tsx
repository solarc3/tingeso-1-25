import { useState } from 'react';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { ReservationResponse } from '@/services/reservationService';
import {
    enviarComprobante,
    ComprobanteResponse,
    descargarComprobantePdf, getComprobanteByReserva
} from '@/services/comprobanteService';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { toast } from "sonner";
import { Mail, Download } from 'lucide-react'; // Iconos

interface AdminReservationViewProps {
    reservations: ReservationResponse[];
}

export default function AdminReservationView({ reservations }: AdminReservationViewProps) {
    const [expandedId, setExpandedId] = useState<number | null>(null);
    const [loadingStates, setLoadingStates] = useState<Record<string, boolean>>({});
    const [comprobantes, setComprobantes] = useState<Record<number, ComprobanteResponse | null>>({});

    const toggleExpand = async (id: number) => {
        const newExpandedId = expandedId === id ? null : id;
        setExpandedId(newExpandedId);

        // Si estamos expandiendo y no tenemos el comprobante, intentamos obtenerlo
        if (newExpandedId && !comprobantes[id]) {
            await fetchComprobante(id);
        }
    };

    const fetchComprobante = async (reservaId: number) => {
        setLoadingStates(prev => ({ ...prev, [`fetch-${reservaId}`]: true }));
        try {
            const data = await getComprobanteByReserva(reservaId);
            setComprobantes(prev => ({ ...prev, [reservaId]: data }));
        } catch (error) {
            console.error('Error obteniendo comprobante:', error);
            setComprobantes(prev => ({ ...prev, [reservaId]: null }));
        } finally {
            setLoadingStates(prev => ({ ...prev, [`fetch-${reservaId}`]: false }));
        }
    };

    const handleEnviarComprobante = async (comprobanteId: number) => {
        if (!comprobanteId) return;

        setLoadingStates(prev => ({ ...prev, [`send-${comprobanteId}`]: true }));
        try {
            await enviarComprobante(comprobanteId);
            toast.success("Comprobante enviado correctamente");

            // Actualizar el estado del comprobante a enviado
            setComprobantes(prev => {
                const comprobante = prev[comprobanteId];
                if (comprobante) {
                    return { ...prev, [comprobanteId]: { ...comprobante, enviado: true } };
                }
                return prev;
            });
        } catch (error) {
            console.error('Error enviando comprobante:', error);
            toast.error("Error al enviar el comprobante");
        } finally {
            setLoadingStates(prev => ({ ...prev, [`send-${comprobanteId}`]: false }));
        }
    };

    const handleDescargarComprobante = async (comprobanteId: number) => {
        if (!comprobanteId) return;

        setLoadingStates(prev => ({ ...prev, [`download-${comprobanteId}`]: true }));
        try {
            await descargarComprobantePdf(comprobanteId);
            toast.success("Comprobante descargado correctamente");
        } catch (error) {
            console.error('Error descargando comprobante:', error);
            toast.error("Error al descargar el comprobante");
        } finally {
            setLoadingStates(prev => ({ ...prev, [`download-${comprobanteId}`]: false }));
        }
    };

    const formatCurrency = (value: number) =>
        new Intl.NumberFormat('es-CL', { style: 'currency', currency: 'CLP' }).format(value);

    if (reservations.length === 0) {
        return (
            <div className="py-8 text-center">
                <p className="text-muted-foreground">No hay reservas para mostrar</p>
            </div>
        );
    }

    return (
        <div className="space-y-4">
            <div className="grid grid-cols-1 gap-4">
                {reservations.map((reservation) => (
                    <Card key={reservation.id} className="overflow-hidden">
                        <div
                            className="bg-muted/50 p-4 flex justify-between items-center cursor-pointer"
                            onClick={() => toggleExpand(reservation.id)}
                        >
                            <div>
                                <p className="font-medium">
                                    {format(new Date(reservation.startTime), 'PPP, HH:mm', { locale: es })}
                                    {' - '}
                                    {format(new Date(reservation.endTime), 'HH:mm')}
                                </p>
                                <p className="text-sm text-muted-foreground">
                                    {reservation.responsibleName} • {reservation.numPeople} personas
                                </p>
                            </div>
                            <div className="flex items-center gap-2">
                                <span className={`px-2 py-1 text-xs rounded-full ${
                                    reservation.status === 'CONFIRMED' ? 'bg-green-100 text-green-800' :
                                        reservation.status === 'PENDING' ? 'bg-yellow-100 text-yellow-800' :
                                            'bg-red-100 text-red-800'
                                }`}>
                                {reservation.status === 'CONFIRMED' ? 'Confirmada' :
                                    reservation.status === 'PENDING' ? 'Pendiente' : 'Cancelada'}
                                </span>
                                <span className="text-sm font-medium">{formatCurrency(reservation.totalAmount)}</span>
                            </div>
                        </div>

                        {expandedId === reservation.id && (
                            <CardContent className="pt-4">
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">

                                    {/* 1) Columna Karts Asignados */}
                                    <div>
                                        <h3 className="font-semibold mb-2">Karts Asignados</h3>
                                        <div className="grid grid-cols-3 gap-2">
                                            {reservation.kartIds.map((kartId, idx) => (
                                                <div key={idx} className="bg-muted p-2 rounded text-center text-sm">
                                                    {kartId}
                                                </div>
                                            ))}
                                        </div>
                                    </div>

                                    {/* 2) Columna Participantes */}
                                    <div>
                                        <h3 className="font-semibold mb-2">Participantes</h3>
                                        <div className="max-h-60 overflow-y-auto border rounded-md">
                                            <table className="min-w-full divide-y divide-gray-200">
                                                <thead className="bg-muted">
                                                <tr>
                                                    <th className="px-3 py-2 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                                                        Nombre
                                                    </th>
                                                    <th className="px-3 py-2 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                                                        Email
                                                    </th>
                                                </tr>
                                                </thead>
                                                <tbody className="bg-white divide-y divide-gray-200">
                                                {reservation.guests.map((guest, idx) => (
                                                    <tr key={idx}>
                                                        <td className="px-3 py-2 text-sm">{guest.name}</td>
                                                        <td className="px-3 py-2 text-sm text-muted-foreground">{guest.email}</td>
                                                    </tr>
                                                ))}
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>

                                    {/* 3) Sección Comprobante ocupa ambas columnas y va abajo */}
                                    <div className="col-span-2 border-t pt-4">
                                        <h3 className="font-semibold mb-2">Comprobante de Pago</h3>

                                        {loadingStates[`fetch-${reservation.id}`] ? (
                                            <div className="flex items-center space-x-2">
                                                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-primary"></div>
                                                <span className="text-sm">Verificando comprobante...</span>
                                            </div>
                                        ) : comprobantes[reservation.id] ? (
                                            <div className="space-y-2">
                                                <div className="text-sm">
                                                    <p>
                                                        <span className="text-muted-foreground">Código:</span>{' '}
                                                        {comprobantes[reservation.id]?.codigoReserva}
                                                    </p>
                                                    <p>
                                                        <span className="text-muted-foreground">Fecha Emisión:</span>{' '}
                                                        {format(
                                                            new Date(comprobantes[reservation.id]?.fechaEmision || ''),
                                                            'PPP, HH:mm',
                                                            { locale: es }
                                                        )}
                                                    </p>
                                                    <p>
                                                        <span className="text-muted-foreground">Estado:</span>{' '}
                                                        {comprobantes[reservation.id]?.enviado ? 'Enviado' : 'Pendiente'}
                                                    </p>
                                                </div>

                                                <div className="flex flex-wrap gap-2">
                                                    <Button
                                                        variant="outline"
                                                        size="sm"
                                                        className="flex gap-1"
                                                        onClick={(e) => {
                                                            e.stopPropagation();
                                                            handleDescargarComprobante(comprobantes[reservation.id]?.id || 0);
                                                        }}
                                                        disabled={loadingStates[`download-${comprobantes[reservation.id]?.id}`]}
                                                    >
                                                        <Download className="h-4 w-4" />
                                                        {loadingStates[`download-${comprobantes[reservation.id]?.id}`]
                                                            ? 'Descargando...'
                                                            : 'Descargar PDF'}
                                                    </Button>
                                                    <Button
                                                        size="sm"
                                                        className="flex gap-1"
                                                        onClick={(e) => {
                                                            e.stopPropagation();
                                                            handleEnviarComprobante(comprobantes[reservation.id]?.id || 0);
                                                        }}
                                                        disabled={loadingStates[`send-${comprobantes[reservation.id]?.id}`]}
                                                    >
                                                        <Mail className="h-4 w-4" />
                                                        {loadingStates[`send-${comprobantes[reservation.id]?.id}`]
                                                            ? 'Enviando...'
                                                            : 'Reenviar por Email'}
                                                    </Button>
                                                </div>
                                            </div>
                                        ) : (
                                            <div className="text-sm text-muted-foreground">
                                                No se encontró un comprobante para esta reserva.
                                            </div>
                                        )}
                                    </div>
                                </div>
                            </CardContent>
                        )}
                    </Card>
                ))}
            </div>
        </div>
    );
}