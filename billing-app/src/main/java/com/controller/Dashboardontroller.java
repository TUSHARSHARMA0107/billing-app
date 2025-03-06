@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @GetMapping("/profit-loss")
    public ResponseEntity<Map<String, Double>> getProfitLoss(@RequestParam Long userId) {
        Double totalIncome = invoiceRepository.getTotalIncome(userId);
        Double totalExpenses = expenseRepository.getTotalExpenses(userId);
        
        totalIncome = (totalIncome != null) ? totalIncome : 0.0;
        totalExpenses = (totalExpenses != null) ? totalExpenses : 0.0;
        
        double profitLoss = totalIncome - totalExpenses;

        Map<String, Double> response = new HashMap<>();
        response.put("Total Income", totalIncome);
        response.put("Total Expenses", totalExpenses);
        response.put("Profit/Loss", profitLoss);

        return ResponseEntity.ok(response);
    }
}