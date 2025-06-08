import { useState, useEffect } from 'react';
import { getReservations, ReservationResponse } from '@/services/reservationService';
import { getRevenueByLapsReport, getRevenueByPeopleReport, ReportResponse } from '@/services/reportService';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import AdminReservationView from '@/components/features/reservation/AdminReservationView';
import PriceConfigPanel from '@/components/features/admin/PriceConfigPanel';
import ReportTable from '@/components/features/admin/ReportTable';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { format, subDays, addDays } from 'date-fns';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { toast } from "sonner";
import WeeklyScheduleRack from '@/components/features/admin/WeeklyScheduleRack';

export default function AdminPage() {
    const [reservations, setReservations] = useState<ReservationResponse[]>([]);
    const [isLoadingReservations, setIsLoadingReservations] = useState(true);
    const [startDate, setStartDate] = useState<Date>(subDays(new Date(), 7));
    const [endDate, setEndDate] = useState<Date>(addDays(new Date(), 7));

    const [lapReport, setLapReport] = useState<ReportResponse | null>(null);
    const [peopleReport, setPeopleReport] = useState<ReportResponse | null>(null);
    const [isReportsLoading, setIsReportsLoading] = useState(false);

    useEffect(() => {
        fetchReservations();
    }, []);

    const fetchReservations = async () => {
        setIsLoadingReservations(true);
        try {
            const data = await getReservations(startDate, endDate);
            setReservations(data);
        } catch (error) {
            console.error('Error fetching reservations:', error);
            toast.error("Error al cargar las reservas");
        } finally {
            setIsLoadingReservations(false);
        }
    };

    const fetchReports = async () => {
        setIsReportsLoading(true);
        try {
            const [lapData, peopleData] = await Promise.all([
                getRevenueByLapsReport(startDate, endDate),
                getRevenueByPeopleReport(startDate, endDate)
            ]);
            setLapReport(lapData);
            setPeopleReport(peopleData);
        } catch (error) {
            console.error('Error fetching reports:', error);
            toast.error("Error al cargar los reportes");
        } finally {
            setIsReportsLoading(false);
        }
    };

    const handleDateChange = (e: React.ChangeEvent<HTMLInputElement>, isStart: boolean) => {
        const date = new Date(e.target.value);
        if (!isNaN(date.getTime())) {
            if (isStart) {
                setStartDate(date);
            } else {
                setEndDate(date);
            }
        }
    };

    return (
        <div className="container mx-auto py-8">
            <h1 className="text-3xl font-bold mb-6">Panel de Administración</h1>

            <Tabs defaultValue="reservations">
                <TabsList className="mb-8">
                    <TabsTrigger value="reservations">Reservas</TabsTrigger>
                    <TabsTrigger value="schedule">Rack Semanal</TabsTrigger>
                    <TabsTrigger value="reports">Reportes</TabsTrigger>
                    <TabsTrigger value="prices">Precios</TabsTrigger>
                </TabsList>

                <TabsContent value="reservations">
                    <Card className="mb-8">
                        <CardHeader>
                            <CardTitle>Filtrar Reservas</CardTitle>
                        </CardHeader>
                        <CardContent>
                            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                <div>
                                    <Label htmlFor="start-date">Fecha Inicial</Label>
                                    <Input
                                        id="start-date"
                                        type="date"
                                        value={format(startDate, 'yyyy-MM-dd')}
                                        onChange={(e) => handleDateChange(e, true)}
                                        className="mt-1"
                                    />
                                </div>
                                <div>
                                    <Label htmlFor="end-date">Fecha Final</Label>
                                    <Input
                                        id="end-date"
                                        type="date"
                                        value={format(endDate, 'yyyy-MM-dd')}
                                        onChange={(e) => handleDateChange(e, false)}
                                        className="mt-1"
                                    />
                                </div>
                                <div className="flex items-end">
                                    <Button onClick={fetchReservations} className="w-full">
                                        Buscar Reservas
                                    </Button>
                                </div>
                            </div>
                        </CardContent>
                    </Card>

                    {isLoadingReservations ? (
                        <div className="text-center py-8">
                            <p>Cargando reservas...</p>
                        </div>
                    ) : (
                        <Card>
                            <CardHeader>
                                <CardTitle>Reservas ({reservations.length})</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <AdminReservationView reservations={reservations} />
                            </CardContent>
                        </Card>
                    )}
                </TabsContent>

                <TabsContent value="reports">
                    <Card className="mb-8">
                        <CardHeader>
                            <CardTitle>Mostrar Reportes</CardTitle>
                        </CardHeader>
                        <CardContent>
                            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                <div>
                                    <Label htmlFor="report-start-date">Fecha Inicial</Label>
                                    <Input
                                        id="report-start-date"
                                        type="date"
                                        value={format(startDate, 'yyyy-MM-dd')}
                                        onChange={(e) => handleDateChange(e, true)}
                                        className="mt-1"
                                    />
                                </div>
                                <div>
                                    <Label htmlFor="report-end-date">Fecha Final</Label>
                                    <Input
                                        id="report-end-date"
                                        type="date"
                                        value={format(endDate, 'yyyy-MM-dd')}
                                        onChange={(e) => handleDateChange(e, false)}
                                        className="mt-1"
                                    />
                                </div>
                                <div className="flex items-end">
                                    <Button onClick={fetchReports} className="w-full">
                                        Generar Reportes
                                    </Button>
                                </div>
                            </div>
                        </CardContent>
                    </Card>

                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                        <ReportTable
                            report={lapReport}
                            isLoading={isReportsLoading}
                        />
                        <ReportTable
                            report={peopleReport}
                            isLoading={isReportsLoading}
                        />
                    </div>
                </TabsContent>

                <TabsContent value="prices">
                    <PriceConfigPanel />
                </TabsContent>
                <TabsContent value="schedule">
                    <WeeklyScheduleRack />
                </TabsContent>
            </Tabs>
        </div>
    );
}