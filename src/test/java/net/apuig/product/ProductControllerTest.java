package net.apuig.product;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import net.apuig.StoreApplication;

@SpringBootTest(classes = StoreApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeAll
    public void setup()
    {
        // already running EntitySetup. Categories and Products
    }

    @Test
    public void listCategories() throws Exception
    {
        this.mockMvc.perform(get("/categories")//
            .param("nameLike", "ffee")//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isOk())//
            .andExpect(jsonPath("$.page.number").value("0"))
            .andExpect(jsonPath("$.page.size").value("10"))
            .andExpect(jsonPath("$.page.totalPages").value("1"))
            .andExpect(jsonPath("$.page.totalElements").value("1"))
            .andExpect(jsonPath("$.content[0].name").value("COFFEE"))
            .andExpect(jsonPath("$.content[0].parentName").value("HOT_BEVERAGES"))
            .andExpect(jsonPath("$.content[0].id").exists())
            .andExpect(jsonPath("$.content[0].parentId").exists());
    }

    @Test
    public void listCategoriesFromParentCategory() throws Exception
    {
        // Coffee can be found from food and beverage
        this.mockMvc
            .perform(
                get("/categories/" + categoryRepository.findByName("FOOD_AND_BEVERAGE").getId())//
                    .param("nameLike", "ffee")//
                    .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isOk())//
            .andExpect(jsonPath("$.page.number").value("0"))
            .andExpect(jsonPath("$.page.size").value("10"))
            .andExpect(jsonPath("$.page.totalPages").value("1"))
            .andExpect(jsonPath("$.page.totalElements").value("1"))
            .andExpect(jsonPath("$.content[0].name").value("COFFEE"))
            .andExpect(jsonPath("$.content[0].parentName").value("HOT_BEVERAGES"))
            .andExpect(jsonPath("$.content[0].id").exists())
            .andExpect(jsonPath("$.content[0].parentId").exists());

        // Coffee can not be found from food
        this.mockMvc.perform(get("/categories/" + categoryRepository.findByName("FOOD").getId())//
            .param("nameLike", "ffee")//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isOk())//
            .andExpect(jsonPath("$.page.number").value("0"))
            .andExpect(jsonPath("$.page.size").value("10"))
            .andExpect(jsonPath("$.page.totalPages").value("0"))
            .andExpect(jsonPath("$.page.totalElements").value("0"));
    }

    @Test
    public void listProducts() throws Exception
    {
        this.mockMvc.perform(get("/products")//
            .param("nameLike", "esso")//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isOk())//
            .andExpect(jsonPath("$.page.number").value("0"))
            .andExpect(jsonPath("$.page.size").value("10"))
            .andExpect(jsonPath("$.page.totalPages").value("1"))
            .andExpect(jsonPath("$.page.totalElements").value("1"))
            .andExpect(jsonPath("$.content[0].name").value("Espresso"))
            .andExpect(jsonPath("$.content[0].id").exists())
            .andExpect(jsonPath("$.content[0].price").value("2.5"))
            .andExpect(jsonPath("$.content[0].currency").value("EURO"))
            .andExpect(jsonPath("$.content[0].category.name").value("COFFEE"))
            .andExpect(jsonPath("$.content[0].category.parentName").value("HOT_BEVERAGES"));
    }

    @Test
    public void listProductsFromParentCategory() throws Exception
    {
        // Espresso can be found from food and beverage
        this.mockMvc
            .perform(get("/categories/%d/products"
                .formatted(categoryRepository.findByName("FOOD_AND_BEVERAGE").getId()))//
                    .param("nameLike", "esso")//
                    .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isOk())//
            .andExpect(jsonPath("$.page.number").value("0"))
            .andExpect(jsonPath("$.page.size").value("10"))
            .andExpect(jsonPath("$.page.totalPages").value("1"))
            .andExpect(jsonPath("$.page.totalElements").value("1"))
            .andExpect(jsonPath("$.content[0].name").value("Espresso"))
            .andExpect(jsonPath("$.content[0].id").exists());

        // Espresso can not be found from food
        this.mockMvc.perform(
            get("/categories/%d/products".formatted(categoryRepository.findByName("FOOD").getId()))//
                .param("nameLike", "esso")//
                .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isOk())//
            .andExpect(jsonPath("$.page.number").value("0"))
            .andExpect(jsonPath("$.page.size").value("10"))
            .andExpect(jsonPath("$.page.totalPages").value("0"))
            .andExpect(jsonPath("$.page.totalElements").value("0"));
    }

    @Test
    @Transactional
    public void listProductsNotAvailable() throws Exception
    {
        Product expresso = productRepository.findByName("Espresso");
        expresso.setAvailable(0);

        this.mockMvc.perform(get("/products")//
            .param("nameLike", "esso")//
            .accept(MediaType.APPLICATION_JSON))//
            .andDo(print()).andExpect(status().isOk())//
            .andExpect(jsonPath("$.page.totalElements").value("0"));
    }
}
