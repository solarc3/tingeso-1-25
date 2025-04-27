import { useState } from 'react';
import { Card, CardContent } from '@/components/ui/card';
import { ReservationResponse } from '@/services/reservationService';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

interface AdminReservationViewProps {
    reservations: ReservationResponse[];
}

export default function AdminReservationView({ reservations }: AdminReservationViewProps) {
    const [expandedId, setExpandedId] = useState<number | null>(null);

    const toggleExpand = (id: number) => {
        setExpandedId(expandedId === id ? null : id);
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
                                    <div>
                                        <h3 className="font-semibold mb-2">Detalles de la Reserva</h3>
                                        <ul className="space-y-1 text-sm">
                                            <li><span className="text-muted-foreground">ID:</span> {reservation.id}</li>
                                            <li><span className="text-muted-foreground">Responsable:</span> {reservation.responsibleName}</li>
                                            <li><span className="text-muted-foreground">Email:</span> {reservation.responsibleEmail}</li>
                                            <li><span className="text-muted-foreground">Estado:</span> {reservation.status}</li>
                                            <li><span className="text-muted-foreground">Precio Base:</span> {formatCurrency(reservation.baseRate)}</li>
                                            <li><span className="text-muted-foreground">Descuento Grupo:</span> {formatCurrency(reservation.groupDiscount)}</li>
                                            <li><span className="text-muted-foreground">Descuento Frecuencia:</span> {formatCurrency(reservation.frequencyDiscount)}</li>
                                            <li><span className="text-muted-foreground">Descuento Cumpleaños:</span> {formatCurrency(reservation.birthdayDiscount)}</li>
                                            <li><span className="text-muted-foreground">IVA:</span> {formatCurrency(reservation.tax)}</li>
                                            <li><span className="text-muted-foreground">Total:</span> {formatCurrency(reservation.totalAmount)}</li>
                                        </ul>
                                    </div>

                                    <div className="space-y-4">
                                        <div>
                                            <h3 className="font-semibold mb-2">Karts Asignados</h3>
                                            <div className="grid grid-cols-3 gap-2">
                                                {reservation.kartIds.map((kartId, index) => (
                                                    <div key={index} className="bg-muted p-2 rounded text-center text-sm">
                                                        {kartId}
                                                    </div>
                                                ))}
                                            </div>
                                        </div>

                                        <div>
                                            <h3 className="font-semibold mb-2">Participantes</h3>
                                            <div className="max-h-60 overflow-y-auto border rounded-md">
                                                <table className="min-w-full divide-y divide-gray-200">
                                                    <thead className="bg-muted">
                                                    <tr>
                                                        <th className="px-3 py-2 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">Nombre</th>
                                                        <th className="px-3 py-2 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">Email</th>
                                                    </tr>
                                                    </thead>
                                                    <tbody className="bg-white divide-y divide-gray-200">
                                                    {reservation.guests.map((guest, index) => (
                                                        <tr key={index}>
                                                            <td className="px-3 py-2 text-sm">{guest.name}</td>
                                                            <td className="px-3 py-2 text-sm text-muted-foreground">{guest.email}</td>
                                                        </tr>
                                                    ))}
                                                    </tbody>
                                                </table>
                                            </div>
                                        </div>
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