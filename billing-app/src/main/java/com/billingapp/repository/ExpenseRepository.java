@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId")
    Double getTotalExpenses(@Param("userId") Long userId);
}