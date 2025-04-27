import { useState, useEffect } from 'react';
import { Calendar } from '@/components/ui/calendar';
import { getReservations, ReservationResponse } from '@/services/reservationService.ts';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

export default function ReservationCalendar() {
    const [date, setDate] = useState<Date | undefined>(new Date());
    const [reservations, setReservations] = useState<ReservationResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [selectedDayReservations, setSelectedDayReservations] = useState<ReservationResponse[]>([]);

    // Efecto para cargar reservas cuando cambia la fecha
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

                // Filtrar reservas del día seleccionado
                updateSelectedDayReservations(date, data);
            } catch (error) {
                console.error('Error fetching reservations:', error);
            } finally {
                setIsLoading(false);
            }
        };

        fetchReservations();
    }, [date?.getMonth()]);

    // Actualizamos las reservas del día seleccionado cuando cambia la selección
    const updateSelectedDayReservations = (selectedDate: Date, allReservations = reservations) => {
        const dayReservations = allReservations.filter(res => {
            const resDate = new Date(res.startTime);
            return resDate.getDate() === selectedDate.getDate() &&
                resDate.getMonth() === selectedDate.getMonth() &&
                resDate.getFullYear() === selectedDate.getFullYear();
        });

        setSelectedDayReservations(dayReservations);
    };

    const renderDay = (day: Date) => {
        // Verificamos si hay reservas para este día
        const hasReservations = reservations.some(res => {
            const resDate = new Date(res.startTime);
            return resDate.getDate() === day.getDate() &&
                resDate.getMonth() === day.getMonth() &&
                resDate.getFullYear() === day.getFullYear();
        });

        return (
            <div className="relative w-full h-full flex items-center justify-center">
                <span>{day.getDate()}</span>
                {hasReservations && (
                    <div className="absolute bottom-1 left-1/2 transform -translate-x-1/2 w-1 h-1 bg-primary rounded-full"></div>
                )}
            </div>
        );
    };

    const handleSelect = (newDate: Date | undefined) => {
        if (newDate) {
            setDate(newDate);
            updateSelectedDayReservations(newDate);
        }
    };

    return (
        <div className="space-y-6">
            <div className="flex justify-center">
                <Calendar
                    mode="single"
                    selected={date}
                    onSelect={handleSelect}
                    locale={es}
                    className="border rounded-md shadow"
                />
            </div>

            {isLoading ? (
                <div className="py-4 text-center">Cargando reservas...</div>
            ) : (
                <div className="mt-6">
                    <h3 className="text-lg font-medium mb-3">
                        Reservas para {date && format(date, "d 'de' MMMM, yyyy", { locale: es })}
                    </h3>

                    {selectedDayReservations.length === 0 ? (
                        <p className="text-muted-foreground">No hay reservas para este día</p>
                    ) : (
                        <div className="space-y-2">
                            {selectedDayReservations.map(res => (
                                <div key={res.id} className="p-3 border rounded-md bg-muted/30">
                                    <div className="flex justify-between">
                    <span className="font-medium">
                      {format(new Date(res.startTime), 'HH:mm')} -
                        {format(new Date(res.endTime), 'HH:mm')}
                    </span>
                                        <span className="text-sm bg-primary/10 text-primary px-2 py-0.5 rounded">
                      {res.numPeople} personas
                    </span>
                                    </div>
                                    <div className="text-sm text-muted-foreground mt-1">
                                        Kart: {res.kartId}
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}