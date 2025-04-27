import { Link } from 'react-router-dom';
import ReservationCalendar from '../components/features/reservation/ReservationCalendar';

export default function HomePage() {
    return (
        <div className="container mx-auto py-8">
            <section className="text-center mb-16">
                <h1 className="text-4xl font-bold mb-4">KartingRM</h1>
                <p className="text-xl mb-6">¡Vive la emoción de la velocidad en nuestro kartódromo!</p>
                <Link
                    to="/booking"
                    className="inline-block bg-primary text-primary-foreground px-6 py-3 rounded-md shadow-sm hover:bg-primary/90 transition-colors"
                >
                    Reservar Ahora
                </Link>
            </section>

            <section className="mt-12">
                <h2 className="text-2xl font-semibold mb-6">Calendario de Reservas</h2>
                <p className="mb-4">Consulta la disponibilidad actual de nuestro kartódromo:</p>
                <div className="bg-card rounded-lg shadow-md p-4">
                    <ReservationCalendar />
                </div>
            </section>
        </div>
    );
}