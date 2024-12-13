package net.apuig;

import static net.apuig.product.CurrencyType.EURO;
import static net.apuig.user.UserType.ATTENDANT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import net.apuig.product.Category;
import net.apuig.product.CategoryRepository;
import net.apuig.product.Product;
import net.apuig.product.ProductRepository;
import net.apuig.user.User;
import net.apuig.user.UserRepository;

@Component
public class EntitySetup implements CommandLineRunner
{
    private static final Logger LOG = LoggerFactory.getLogger(EntitySetup.class);

    private final CategoryRepository categoryRepository;

    private final ProductRepository productRepository;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public EntitySetup(CategoryRepository categoryRepository, ProductRepository productRepository,
        UserRepository userRepository, PasswordEncoder passwordEncoder)
    {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception
    {
        setupCategoriesAndProducts();
        setupAttendants();
        LOG.info("Initial entities ready");
    }

    public void setupAttendants()
    {
        userRepository.save(new User("att1", passwordEncoder.encode("Att1Passowrd!"), ATTENDANT));
        userRepository.save(new User("att2", passwordEncoder.encode("Att2Passowrd!"), ATTENDANT));
    }

    public void setupCategoriesAndProducts()
    {
        // Root Category
        Category categoryAll = categoryRepository.save(new Category("ALL", null));

        // 1. Comfort
        Category categoryComfort = categoryRepository.save(new Category("COMFORT", categoryAll));

        // 1.1 Sleep
        Category categorySleep = categoryRepository.save(new Category("SLEEP", categoryComfort));
        Category categoryNeckPillows =
            categoryRepository.save(new Category("NECK_PILLOWS", categorySleep));

        productRepository
            .save(new Product("Memory Foam Neck Pillow", 24.99f, EURO, categoryNeckPillows, 100));
        productRepository
            .save(new Product("Inflatable Neck Pillow", 12.50f, EURO, categoryNeckPillows, 100));

        // 2. Food and Beverage
        Category categoryFoodAndBeverage =
            categoryRepository.save(new Category("FOOD_AND_BEVERAGE", categoryAll));

        // 2.1 Food Categories
        Category categoryFood =
            categoryRepository.save(new Category("FOOD", categoryFoodAndBeverage));

        // 2.1.1 Savory Food
        Category categorySavoryFood =
            categoryRepository.save(new Category("SAVORY_FOOD", categoryFood));
        Category categorySandwiches =
            categoryRepository.save(new Category("SANDWICHES", categorySavoryFood));
        productRepository
            .save(new Product("Chicken Club Sandwich", 8.50f, EURO, categorySandwiches, 100));
        productRepository
            .save(new Product("Vegetarian Wrap", 7.25f, EURO, categorySandwiches, 100));

        Category categorySalads =
            categoryRepository.save(new Category("SALADS", categorySavoryFood));
        productRepository.save(new Product("Caesar Salad", 6.99f, EURO, categorySalads, 100));
        productRepository.save(new Product("Greek Salad", 6.75f, EURO, categorySalads, 100));

        // 2.1.2 Sweet Food
        Category categorySweetFood =
            categoryRepository.save(new Category("SWEET_FOOD", categoryFood));
        Category categoryCakes = categoryRepository.save(new Category("CAKES", categorySweetFood));
        productRepository.save(new Product("Mini Chocolate Cake", 4.50f, EURO, categoryCakes, 100));
        productRepository.save(new Product("Mini Cheesecake", 4.75f, EURO, categoryCakes, 100));

        Category categoryCookies =
            categoryRepository.save(new Category("COOKIES", categorySweetFood));
        productRepository
            .save(new Product("Chocolate Chip Cookies", 2.25f, EURO, categoryCookies, 100));
        productRepository
            .save(new Product("Oatmeal Raisin Cookies", 2.00f, EURO, categoryCookies, 100));

        // 2.2 Beverage Categories
        Category categoryBeverages =
            categoryRepository.save(new Category("BEVERAGES", categoryFoodAndBeverage));

        // 2.2.1 Hot Beverages
        Category categoryHotBeverages =
            categoryRepository.save(new Category("HOT_BEVERAGES", categoryBeverages));
        Category categoryCoffee =
            categoryRepository.save(new Category("COFFEE", categoryHotBeverages));
        productRepository.save(new Product("Espresso", 2.50f, EURO, categoryCoffee, 100));
        productRepository.save(new Product("Cappuccino", 2.25f, EURO, categoryCoffee, 100));

        Category categoryTea = categoryRepository.save(new Category("TEA", categoryHotBeverages));
        productRepository.save(new Product("English Breakfast Tea", 2.00f, EURO, categoryTea, 100));
        productRepository.save(new Product("Green Tea", 2.25f, EURO, categoryTea, 100));

        // 2.2.2 Cold Beverages
        Category categoryColdBeverages =
            categoryRepository.save(new Category("COLD_BEVERAGES", categoryBeverages));
        Category categorySoftDrinks =
            categoryRepository.save(new Category("SOFT_DRINKS", categoryColdBeverages));
        productRepository.save(new Product("Cola", 2.75f, EURO, categorySoftDrinks, 100));
        productRepository.save(new Product("Lemon Soda", 2.50f, EURO, categorySoftDrinks, 100));

        Category categoryJuices =
            categoryRepository.save(new Category("JUICES", categoryColdBeverages));
        productRepository.save(new Product("Orange Juice", 2.25f, EURO, categoryJuices, 100));
        productRepository.save(new Product("Apple Juice", 2.00f, EURO, categoryJuices, 100));
    }

    public void cleanup()
    {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }
}
