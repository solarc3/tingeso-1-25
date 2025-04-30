import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { getReservations, ReservationResponse, cancelReservation as cancelReservationApi } from '@/services/reservationService';
import { addDays, format, startOfWeek, endOfWeek, addMinutes, isSameDay, parseISO } from 'date-fns';
import { es } from 'date-fns/locale';
import { X, ChevronRight, ChevronLeft } from 'lucide-react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { toast } from "sonner";

export default function WeeklyScheduleRack() {
    const [currentWeekStart, setCurrentWeekStart] = useState(startOfWeek(new Date(), { weekStartsOn: 1 }));
    const [reservations, setReservations] = useState<ReservationResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [selectedReservations, setSelectedReservations] = useState<ReservationResponse[]>([]);
    const [showDetails, setShowDetails] = useState(false);
    const [activeReservationIndex, setActiveReservationIndex] = useState(0);

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
    const findReservations = (day: Date, timeSlot: Date) => {
        return reservations.filter(res => {
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

    const handleReservationClick = (blockReservations: ReservationResponse[]) => {
        setSelectedReservations(blockReservations);
        setActiveReservationIndex(0);
        setShowDetails(true);
    };

    const formatCurrency = (value: number) =>
        new Intl.NumberFormat('es-CL', { style: 'currency', currency: 'CLP' }).format(value);

    const timeSlots = getTimeSlots();

    const nextReservation = () => {
        setActiveReservationIndex((prev) =>
            prev < selectedReservations.length - 1 ? prev + 1 : prev
        );
    };

    const prevReservation = () => {
        setActiveReservationIndex((prev) =>
            prev > 0 ? prev - 1 : 0
        );
    };

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
                                            const blockReservations = findReservations(day, timeSlot);
                                            const isAvailable = blockReservations.length === 0;
                                            const hasMultiple = blockReservations.length > 1;

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
                                                    {blockReservations.length > 0 && (
                                                        <button
                                                            onClick={() => handleReservationClick(blockReservations)}
                                                            className="absolute inset-0 w-full h-full flex items-center justify-center hover:bg-muted/30 p-1"
                                                        >
                                                            <div className="text-xs w-full overflow-hidden">
                                                                <div className="font-bold truncate">
                                                                    {hasMultiple
                                                                        ? `${blockReservations.length} reservas`
                                                                        : `${blockReservations[0].numPeople} karts`}
                                                                </div>
                                                                <div className="truncate">
                                                                    {hasMultiple
                                                                        ? `Múltiples clientes`
                                                                        : blockReservations[0].responsibleName}
                                                                </div>
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

            {selectedReservations.length > 0 && (
                <Dialog open={showDetails} onOpenChange={setShowDetails}>
                    <DialogContent className="max-w-3xl">
                        <div className="sticky top-0 bg-background z-10">
                            <DialogHeader className="mb-4">
                                <div className="flex justify-between items-center">
                                    <DialogTitle>Detalles de la Reserva</DialogTitle>
                                    {selectedReservations.length > 1 && (
                                        <div className="flex items-center space-x-2">
                                            <Button
                                                variant="outline"
                                                size="icon"
                                                onClick={prevReservation}
                                                disabled={activeReservationIndex === 0}
                                                className="h-8 w-8"
                                            >
                                                <ChevronLeft className="h-4 w-4" />
                                            </Button>
                                            <span className="text-sm font-medium">
                                                {activeReservationIndex + 1} de {selectedReservations.length}
                                            </span>
                                            <Button
                                                variant="outline"
                                                size="icon"
                                                onClick={nextReservation}
                                                disabled={activeReservationIndex === selectedReservations.length - 1}
                                                className="h-8 w-8"
                                            >
                                                <ChevronRight className="h-4 w-4" />
                                            </Button>
                                        </div>
                                    )}
                                </div>
                            </DialogHeader>
                        </div>
                        <div className="flex flex-col" style={{ minHeight: "400px" }}>
                            {selectedReservations.length > 0 && (
                                <div className="space-y-4">
                                    <div className="grid grid-cols-2 gap-4 h-40">
                                        <div>
                                            <p className="text-sm text-muted-foreground">Responsable</p>
                                            <p className="font-medium">{selectedReservations[activeReservationIndex].responsibleName}</p>
                                        </div>
                                        <div>
                                            <p className="text-sm text-muted-foreground">Email</p>
                                            <p className="font-medium">{selectedReservations[activeReservationIndex].responsibleEmail}</p>
                                        </div>
                                        <div>
                                            <p className="text-sm text-muted-foreground">Horario</p>
                                            <p className="font-medium">
                                                {format(parseISO(selectedReservations[activeReservationIndex].startTime), 'HH:mm')} -
                                                {format(parseISO(selectedReservations[activeReservationIndex].endTime), 'HH:mm')}
                                            </p>
                                        </div>
                                        <div>
                                            <p className="text-sm text-muted-foreground">Karts</p>
                                            <p className="font-medium">{selectedReservations[activeReservationIndex].numPeople}</p>
                                        </div>
                                        <div>
                                            <p className="text-sm text-muted-foreground">Monto</p>
                                            <p className="font-medium">{formatCurrency(selectedReservations[activeReservationIndex].totalAmount)}</p>
                                        </div>
                                        <div>
                                            <p className="text-sm text-muted-foreground">Estado</p>
                                            <p className={`font-medium ${
                                                selectedReservations[activeReservationIndex].status === 'CONFIRMED' ? 'text-green-600' :
                                                    selectedReservations[activeReservationIndex].status === 'PENDING' ? 'text-yellow-600' : 'text-red-600'
                                            }`}>
                                                {selectedReservations[activeReservationIndex].status === 'CONFIRMED' ? 'Confirmada' :
                                                    selectedReservations[activeReservationIndex].status === 'PENDING' ? 'Pendiente' : 'Cancelada'}
                                            </p>
                                        </div>
                                    </div>

                                    <div className="h-24">
                                        <h4 className="text-sm font-medium mb-2">Karts asignados</h4>
                                        <div className="flex flex-wrap gap-2">
                                            {selectedReservations[activeReservationIndex].kartIds.map((kartId, idx) => (
                                                <span key={idx} className="bg-muted py-1 px-2 text-xs rounded">
                                                    {kartId}
                                                </span>
                                            ))}
                                        </div>
                                    </div>

                                    <div>
                                        <h4 className="text-sm font-medium mb-2">Invitados</h4>
                                        <div className="max-h-32 overflow-y-auto border rounded p-2">
                                            {selectedReservations[activeReservationIndex].guests.map((guest, idx) => (
                                                <div key={idx} className="py-1 border-b last:border-0 text-sm">
                                                    <div>{guest.name}</div>
                                                    <div className="text-xs text-muted-foreground">{guest.email}</div>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                </div>
                            )}
                        </div>
                        <DialogFooter className="flex justify-between mt-4 pt-4 border-t">
                            <Button
                                variant="outline"
                                onClick={() => setShowDetails(false)}
                            >
                                Cerrar
                            </Button>
                            {selectedReservations[activeReservationIndex].status !== 'CANCELLED' && (
                                <Button
                                    variant="destructive"
                                    onClick={() => cancelReservation(selectedReservations[activeReservationIndex].id)}
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