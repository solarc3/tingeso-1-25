import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { getReservations, ReservationResponse, cancelReservation as cancelReservationApi } from '@/services/reservationService';
import { addDays, format, startOfWeek, endOfWeek, addMinutes, isSameDay, parseISO } from 'date-fns';
import { es } from 'date-fns/locale';
import { X } from 'lucide-react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { toast } from 'sonner';

export default function WeeklyScheduleRack() {
    const [currentWeekStart, setCurrentWeekStart] = useState(startOfWeek(new Date(), { weekStartsOn: 1 }));
    const [reservations, setReservations] = useState<ReservationResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [selectedReservation, setSelectedReservation] = useState<ReservationResponse | null>(null);
    const [showDetails, setShowDetails] = useState(false);

    // Horarios unificados desde 10:00 AM hasta 9:30 PM
    const startHour = 10;   // 10:00 AM para todos los días
    const endHour = 21.5;   // 9:30 PM para todos los días
    const interval = 30;    // Intervalos de 30 minutos

    useEffect(() => {
        loadWeekReservations();
    }, [currentWeekStart]);

    const loadWeekReservations = async () => {
        setIsLoading(true);
        try {
            const weekEnd = endOfWeek(currentWeekStart, { weekStartsOn: 1 });
            const data = await getReservations(currentWeekStart, weekEnd);
            setReservations(data);
        } catch (error) {
            console.error('Error loading weekly reservations:', error);
            toast.error("Error al cargar las reservas de la semana");
        } finally {
            setIsLoading(false);
        }
    };

    const cancelReservation = async (id: number) => {
        try {
            await cancelReservationApi(id);
            toast.success("Reserva cancelada correctamente");
            setShowDetails(false);
            loadWeekReservations();
        } catch (error) {
            console.error('Error al cancelar la reserva:', error);
            toast.error("Error al cancelar la reserva");
        }
    };
    const weekDays = Array.from({ length: 7 }, (_, i) =>
        addDays(currentWeekStart, i)
    );
    const getTimeSlots = () => {
        const slots = [];
        let time = new Date();
        time.setHours(startHour, 0, 0, 0);

        while (time.getHours() + (time.getMinutes() / 60) < endHour) {
            slots.push(new Date(time));
            time = addMinutes(time, interval);
        }

        return slots;
    };
    const findReservation = (day: Date, timeSlot: Date) => {
        return reservations.find(res => {
            const startTime = parseISO(res.startTime);
            const endTime = parseISO(res.endTime);
            const slotTime = new Date(day);
            slotTime.setHours(timeSlot.getHours(), timeSlot.getMinutes(), 0, 0);

            return isSameDay(startTime, slotTime) &&
                slotTime >= startTime &&
                slotTime < endTime;
        });
    };

    const handlePrevWeek = () => {
        setCurrentWeekStart(addDays(currentWeekStart, -7));
    };

    const handleNextWeek = () => {
        setCurrentWeekStart(addDays(currentWeekStart, 7));
    };

    const handleReservationClick = (reservation: ReservationResponse) => {
        setSelectedReservation(reservation);
        setShowDetails(true);
    };

    const formatCurrency = (value: number) =>
        new Intl.NumberFormat('es-CL', { style: 'currency', currency: 'CLP' }).format(value);

    const timeSlots = getTimeSlots();

    return (
        <div className="space-y-4">
            <Card className="shadow-sm">
                <CardHeader className="flex flex-row items-center justify-between pb-2">
                    <CardTitle>Rack Semanal de Ocupación</CardTitle>
                    <div className="flex space-x-2">
                        <Button variant="outline" onClick={handlePrevWeek}>&lt; Semana anterior</Button>
                        <Button variant="default" onClick={() => setCurrentWeekStart(startOfWeek(new Date(), { weekStartsOn: 1 }))}>
                            Hoy
                        </Button>
                        <Button variant="outline" onClick={handleNextWeek}>Próxima semana &gt;</Button>
                    </div>
                </CardHeader>
                <CardContent>
                    {isLoading ? (
                        <div className="flex justify-center p-8">
                            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
                        </div>
                    ) : (
                        <div className="overflow-x-auto">
                            <table className="w-full border-collapse table-fixed">
                                {/* Comentario antes de colgroup: Definición de anchos de columnas */}
                                <colgroup>
                                    <col style={{ width: '80px' }} />
                                    {weekDays.map((_, idx) => (
                                        <col key={idx} style={{ width: 'calc((100% - 80px) / 7)' }} />
                                    ))}
                                </colgroup>
                                <thead>
                                <tr className="bg-muted/50">
                                    <th className="border p-2">Hora</th>
                                    {weekDays.map((day, idx) => (
                                        <th key={idx} className="border p-2">
                                            <div className="font-bold">{format(day, 'EEEE', { locale: es })}</div>
                                            <div className="text-sm">{format(day, 'd MMM', { locale: es })}</div>
                                        </th>
                                    ))}
                                </tr>
                                </thead>
                                <tbody>
                                {timeSlots.map((timeSlot, timeIdx) => (
                                    <tr key={timeIdx} className="h-14 hover:bg-muted/20">
                                        <td className="border p-1 text-center font-medium text-sm">
                                            {format(timeSlot, 'HH:mm')}
                                        </td>

                                        {weekDays.map((day, dayIdx) => {
                                            const reservation = findReservation(day, timeSlot);
                                            const isAvailable = !reservation;

                                            // Resaltar horarios fuera de disponibilidad para días de semana
                                            const isWeekday = ![0, 6].includes(day.getDay());
                                            const isBeforeWeekdayOpen = isWeekday && timeSlot.getHours() < 14;

                                            return (
                                                <td
                                                    key={dayIdx}
                                                    className={`
                                                            border p-1 text-center 
                                                            ${isBeforeWeekdayOpen ? 'bg-gray-100/50' : isAvailable ? 'bg-green-50/50' : 'bg-red-50'} 
                                                            relative
                                                        `}
                                                >
                                                    {reservation && (
                                                        <button
                                                            onClick={() => handleReservationClick(reservation)}
                                                            className="absolute inset-0 w-full h-full flex items-center justify-center hover:bg-muted/30 p-1"
                                                        >
                                                            <div className="text-xs w-full overflow-hidden">
                                                                <div className="font-bold truncate">{reservation.numPeople} karts</div>
                                                                <div className="truncate">{reservation.responsibleName}</div>
                                                            </div>
                                                        </button>
                                                    )}

                                                    {isBeforeWeekdayOpen && (
                                                        <div className="absolute inset-0 flex items-center justify-center opacity-50">
                                                            <span className="text-xs text-gray-400"></span>
                                                        </div>
                                                    )}
                                                </td>
                                            );
                                        })}
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </CardContent>
            </Card>

            {selectedReservation && (
                <Dialog open={showDetails} onOpenChange={setShowDetails}>
                    <DialogContent>
                        <DialogHeader>
                            <DialogTitle>Detalles de la Reserva</DialogTitle>
                        </DialogHeader>

                        <div className="space-y-4 mt-2">
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <p className="text-sm text-muted-foreground">Responsable</p>
                                    <p className="font-medium">{selectedReservation.responsibleName}</p>
                                </div>
                                <div>
                                    <p className="text-sm text-muted-foreground">Email</p>
                                    <p className="font-medium">{selectedReservation.responsibleEmail}</p>
                                </div>
                                <div>
                                    <p className="text-sm text-muted-foreground">Horario</p>
                                    <p className="font-medium">
                                        {format(parseISO(selectedReservation.startTime), 'HH:mm')} -
                                        {format(parseISO(selectedReservation.endTime), 'HH:mm')}
                                    </p>
                                </div>
                                <div>
                                    <p className="text-sm text-muted-foreground">Karts</p>
                                    <p className="font-medium">{selectedReservation.numPeople}</p>
                                </div>
                                <div>
                                    <p className="text-sm text-muted-foreground">Monto</p>
                                    <p className="font-medium">{formatCurrency(selectedReservation.totalAmount)}</p>
                                </div>
                                <div>
                                    <p className="text-sm text-muted-foreground">Estado</p>
                                    <p className={`font-medium ${
                                        selectedReservation.status === 'CONFIRMED' ? 'text-green-600' :
                                            selectedReservation.status === 'PENDING' ? 'text-yellow-600' : 'text-red-600'
                                    }`}>
                                        {selectedReservation.status === 'CONFIRMED' ? 'Confirmada' :
                                            selectedReservation.status === 'PENDING' ? 'Pendiente' : 'Cancelada'}
                                    </p>
                                </div>
                            </div>

                            <div className="pt-2">
                                <h4 className="text-sm font-medium mb-2">Karts asignados</h4>
                                <div className="flex flex-wrap gap-2">
                                    {selectedReservation.kartIds.map((kartId, idx) => (
                                        <span key={idx} className="bg-muted py-1 px-2 text-xs rounded">
                                            {kartId}
                                        </span>
                                    ))}
                                </div>
                            </div>
                        </div>

                        <DialogFooter className="flex justify-between">
                            <Button
                                variant="outline"
                                onClick={() => setShowDetails(false)}
                            >
                                Cerrar
                            </Button>
                            {selectedReservation.status !== 'CANCELLED' && (
                                <Button
                                    variant="destructive"
                                    onClick={() => cancelReservation(selectedReservation.id)}
                                >
                                    <X className="h-4 w-4 mr-2" /> Cancelar Reserva
                                </Button>
                            )}
                        </DialogFooter>
                    </DialogContent>
                </Dialog>
            )}
        </div>
    );
}