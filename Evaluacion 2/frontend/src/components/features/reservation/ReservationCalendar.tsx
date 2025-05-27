import { useState, useEffect } from 'react';
import { Calendar } from '@/components/ui/calendar';
import { getReservations, checkKartAvailability, ReservationResponse, KartAvailabilityResponse } from '@/services/reservationService.ts';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Clock, Users, Calendar as CalendarIcon, Info } from 'lucide-react';
import { Badge } from '@/components/ui/badge';

export default function ReservationCalendar() {
    const [date, setDate] = useState<Date | undefined>(new Date());
    const [reservations, setReservations] = useState<ReservationResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [selectedDayReservations, setSelectedDayReservations] = useState<ReservationResponse[]>([]);
    const [availabilityMap, setAvailabilityMap] = useState<Record<string, KartAvailabilityResponse>>({});

    useEffect(() => {
        const fetchReservations = async () => {
            if (!date) return;

            setIsLoading(true);
            try {
                // Calculamos el primer y último día del mes para obtener todas las reservas
                const firstDayOfMonth = new Date(date.getFullYear(), date.getMonth(), 1);
                const lastDayOfMonth = new Date(date.getFullYear(), date.getMonth() + 1, 0);

                const data = await getReservations(firstDayOfMonth, lastDayOfMonth);
                setReservations(data);
                setReservations(data);
                // Filtrar reservas del día seleccionado
                const dayReservations = filterDayReservations(date, data);
                setSelectedDayReservations(dayReservations);
                setSelectedDayReservations(dayReservations);
                // Obtener la disponibilidad para cada reserva
                if (dayReservations.length > 0) {
                    await fetchAvailabilityForReservations(dayReservations);
                }
            } catch (error) {
                console.error('Error fetching reservations:', error);
            } finally {
                setIsLoading(false);
            }
        };

        fetchReservations();
    }, [date?.getMonth(), date?.getDate()]);

    const fetchAvailabilityForReservations = async (dayReservations: ReservationResponse[]) => {
        const newAvailabilityMap: Record<string, KartAvailabilityResponse> = {};

        // Obtener disponibilidad para cada reserva
        for (const reservation of dayReservations) {
            try {
                const availability = await checkKartAvailability(
                    reservation.startTime,
                    reservation.endTime
                );

                console.log('Availability response:', availability);
                newAvailabilityMap[reservation.id.toString()] = availability;
            } catch (err) {
                console.error(`Error checking availability for reservation ${reservation.id}:`, err);
            }
        }

        setAvailabilityMap(newAvailabilityMap);
    };

    const filterDayReservations = (selectedDate: Date, allReservations = reservations) => {
        return allReservations.filter(res => {
            const resDate = new Date(res.startTime);
            return resDate.getDate() === selectedDate.getDate() &&
                resDate.getMonth() === selectedDate.getMonth() &&
                resDate.getFullYear() === selectedDate.getFullYear();
        });
    };

    const handleSelect = (newDate: Date | undefined) => {
        if (newDate) {
            setDate(newDate);
            const dayReservations = filterDayReservations(newDate);
            setSelectedDayReservations(dayReservations);

            // Limpiar el mapa de disponibilidad anterior y obtener nuevo
            setAvailabilityMap({});
            if (dayReservations.length > 0) {
                fetchAvailabilityForReservations(dayReservations);
            }
        }
    };

    // Función para renderizar el indicador de disponibilidad
    const renderAvailabilityBadge = (reservationId: number) => {
        const availability = availabilityMap[reservationId.toString()];

        if (!availability) {
            return <span className="text-xs text-muted-foreground">Verificando disponibilidad...</span>;
        }

        const { availableKarts, totalKarts } = availability;
        const badgeColor = availableKarts > 5
            ? "bg-green-100 text-green-800"
            : availableKarts > 0
                ? "bg-yellow-100 text-yellow-800"
                : "bg-red-100 text-red-800";

        return (
            <div className="flex items-center mt-2 gap-1">
                <Info className="h-3 w-3 text-muted-foreground" />
                <span className="text-xs text-muted-foreground">Karts disponibles: </span>
                <Badge className={badgeColor}>
                    {availableKarts}/{totalKarts}
                </Badge>
            </div>
        );
    };

    return (
        <div className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8 items-start">
                {/* Calendario con altura fija y sin modifiers */}
                <div className="flex justify-center h-[380px]">
                    <Calendar
                        mode="single"
                        selected={date}
                        onSelect={handleSelect}
                        locale={es}
                        className="h-full border rounded-xl shadow-md p-4 bg-card"
                    />
                </div>

                {/* Panel de reservas con mismo alto */}
                <div className="flex flex-col h-[380px]">
                    <h3 className="text-xl font-medium mb-4 flex items-center">
                        <CalendarIcon className="mr-2 h-5 w-5" />
                        {date && format(date, "d 'de' MMMM, yyyy", { locale: es })}
                    </h3>

                    {isLoading ? (
                        <div className="flex-1 flex items-center justify-center">
                            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
                        </div>
                    ) : (
                        <div className="flex-1 overflow-y-auto pr-2">
                            {selectedDayReservations.length === 0 ? (
                                <div className="bg-muted/30 rounded-lg p-8 text-center h-full flex flex-col items-center justify-center border-2 border-dashed">
                                    <p className="text-muted-foreground mb-4">No hay reservas para este día</p>
                                    <Button asChild>
                                        <a href="/booking">Reservar este día</a>
                                    </Button>
                                </div>
                            ) : (
                                <div className="space-y-3">
                                    {selectedDayReservations.map(res => (
                                        <Card key={res.id} className="border shadow-sm hover:shadow-md transition-all">
                                            <CardContent className="p-4">
                                                <div className="flex justify-between">
                                                    <div className="flex items-center gap-2">
                                                        <Clock className="h-4 w-4 text-muted-foreground" />
                                                        <span className="font-medium">
                                                            {format(new Date(res.startTime), 'HH:mm')} - {format(new Date(res.endTime), 'HH:mm')}
                                                        </span>
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        <Users className="h-4 w-4 text-muted-foreground" />
                                                        <span className="bg-primary/10 text-primary px-2 py-0.5 rounded text-sm font-medium">
                                                            {res.numPeople} personas
                                                        </span>
                                                    </div>
                                                </div>
                                                <div className="mt-2 flex justify-between items-center">
                                                    <span className={`text-sm ${
                                                        res.status === 'CONFIRMED' ? 'text-green-600' :
                                                            res.status === 'PENDING' ? 'text-yellow-600' : 'text-red-600'
                                                    }`}>
                                                        {res.status === 'CONFIRMED' ? 'Confirmada' : res.status === 'PENDING' ? 'Pendiente' : 'Cancelada'}
                                                    </span>
                                                    {renderAvailabilityBadge(res.id)}
                                                </div>
                                            </CardContent>
                                        </Card>
                                    ))}
                                </div>
                            )}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}