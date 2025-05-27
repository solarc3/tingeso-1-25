import { ReportResponse } from '@/services/reportService';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

interface ReportTableProps {
    report: ReportResponse | null;
    isLoading: boolean;
}

export default function ReportTable({ report, isLoading }: ReportTableProps) {
    const formatCurrency = (value: number) =>
        new Intl.NumberFormat('es-CL', { style: 'currency', currency: 'CLP' }).format(value);

    if (isLoading) {
        return (
            <Card>
                <CardContent className="py-6">
                    <div className="text-center">Cargando reporte...</div>
                </CardContent>
            </Card>
        );
    }

    if (!report) {
        return (
            <Card>
                <CardContent className="py-6">
                    <div className="text-center text-muted-foreground">
                        Selecciona un rango de fechas para generar el reporte
                    </div>
                </CardContent>
            </Card>
        );
    }

    return (
        <Card>
            <CardHeader>
                <CardTitle>{report.reportTitle}</CardTitle>
            </CardHeader>
            <CardContent>
                <div className="overflow-x-auto">
                    <table className="w-full border-collapse">
                        <thead>
                        <tr className="bg-muted">
                            <th className="border p-2 text-left">Categoría</th>
                            <th className="border p-2 text-left">Cantidad de Reservas</th>
                            <th className="border p-2 text-left">Ingresos Totales</th>
                        </tr>
                        </thead>
                        <tbody>
                        {report.entries.map((entry, index) => (
                            <tr key={index}>
                                <td className="border p-2">{entry.category}</td>
                                <td className="border p-2 text-center">{entry.count}</td>
                                <td className="border p-2 text-right">{formatCurrency(entry.totalRevenue)}</td>
                            </tr>
                        ))}
                        </tbody>
                        <tfoot>
                        <tr className="font-bold">
                            <td className="border p-2">Total</td>
                            <td className="border p-2 text-center">
                                {report.entries.reduce((sum, entry) => sum + entry.count, 0)}
                            </td>
                            <td className="border p-2 text-right">
                                {formatCurrency(
                                    report.entries.reduce((sum, entry) => sum + entry.totalRevenue, 0)
                                )}
                            </td>
                        </tr>
                        </tfoot>
                    </table>
                </div>
            </CardContent>
        </Card>
    );
}