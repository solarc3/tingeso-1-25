import { useEffect } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import { ReservationResponse } from '@/services/reservationService';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

export default function ConfirmationPage() {
    const location = useLocation();
    const navigate = useNavigate();
    const reservations = location.state?.reservation as ReservationResponse[] | undefined;

    useEffect(() => {
        if (!reservations || reservations.length === 0) {
            navigate('/booking');
        }
    }, [reservations, navigate]);

    if (!reservations || reservations.length === 0) {
        return <div className="container mx-auto py-8 text-center">Redirigiendo...</div>;
    }

    const firstReservation = reservations[0];
    const formatCurrency = (value: number) =>
        new Intl.NumberFormat('es-CL', { style: 'currency', currency: 'CLP' }).format(value);

    const formatDateTime = (dateTime: string) =>
        format(new Date(dateTime), "d 'de' MMMM, yyyy 'a las' HH:mm", { locale: es });

    return (
        <div className="container mx-auto py-8">
            <div className="max-w-3xl mx-auto">
                <div className="mb-8 text-center">
                    <h1 className="text-3xl font-bold mb-2">¡Reserva Confirmada!</h1>
                    <p className="text-muted-foreground">Tu reserva ha sido registrada correctamente.</p>
                </div>

                <Card className="mb-6">
                    <CardHeader>
                        <CardTitle>Detalles de la Reserva</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div>
                                <p className="text-sm text-muted-foreground">Fecha y Hora</p>
                                <p className="font-medium">{formatDateTime(firstReservation.startTime)}</p>
                            </div>
                            <div>
                                <p className="text-sm text-muted-foreground">Duración</p>
                                <p className="font-medium">
                                    {format(new Date(firstReservation.startTime), 'HH:mm')} -
                                    {format(new Date(firstReservation.endTime), 'HH:mm')}
                                </p>
                            </div>
                            <div>
                                <p className="text-sm text-muted-foreground">Número de Personas</p>
                                <p className="font-medium">{firstReservation.numPeople}</p>
                            </div>
                            <div>
                                <p className="text-sm text-muted-foreground">Monto Total</p>
                                <p className="font-medium text-lg">{formatCurrency(firstReservation.totalAmount)}</p>
                            </div>
                        </div>
                    </CardContent>
                </Card>

                <Card className="mb-8">
                    <CardHeader>
                        <CardTitle>Karts Asignados</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="grid grid-cols-2 md:grid-cols-4 gap-2">
                            {reservations.map((res) => (
                                <div key={res.id} className="bg-muted p-3 rounded-md text-center">
                                    <p className="font-semibold">{res.kartId}</p>
                                </div>
                            ))}
                        </div>
                    </CardContent>
                </Card>

                <div className="flex flex-col sm:flex-row gap-4 justify-center">
                    <Button asChild variant="outline">
                        <Link to="/">Volver al Inicio</Link>
                    </Button>
                    <Button asChild>
                        <Link to="/booking">Hacer Otra Reserva</Link>
                    </Button>
                </div>
            </div>
        </div>
    );
}